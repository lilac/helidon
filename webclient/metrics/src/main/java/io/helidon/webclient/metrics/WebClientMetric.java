/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.webclient.metrics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import io.helidon.common.config.Config;
import io.helidon.http.Method;
import io.helidon.metrics.api.MeterRegistry;
import io.helidon.metrics.api.Metrics;
import io.helidon.webclient.api.WebClientServiceRequest;
import io.helidon.webclient.api.WebClientServiceResponse;
import io.helidon.webclient.spi.WebClientService;

abstract class WebClientMetric implements WebClientService {

    private static final int ERROR_STATUS_CODE = 400;

    private final MeterRegistry registry;
    private final Set<String> methods;
    private final String nameFormat;
    private final String description;
    private final boolean success;
    private final boolean errors;

    WebClientMetric(Builder builder) {
        this.registry = Metrics.globalRegistry();
        this.methods = builder.methods;
        this.nameFormat = builder.nameFormat;
        this.description = builder.description;
        this.success = builder.success;
        this.errors = builder.errors;
    }

    static Builder builder(WebClientMetricType clientMetricType) {
        return new Builder(clientMetricType);
    }

    MeterRegistry meterRegistry() {
        return registry;
    }

    Set<String> methods() {
        return methods;
    }

    String nameFormat() {
        return nameFormat;
    }

    String description() {
        return description;
    }

    boolean measureSuccess() {
        return success;
    }

    boolean measureErrors() {
        return errors;
    }

    boolean shouldContinueOnSuccess(Method method, int status) {
        return handlesMethod(method) && measureSuccess() && status < ERROR_STATUS_CODE;
    }

    boolean shouldContinueOnError(Method method) {
        return handlesMethod(method) && measureErrors();
    }

    boolean shouldContinueOnError(Method method, int status) {
        if (status >= ERROR_STATUS_CODE) {
            return shouldContinueOnError(method);
        }
        return false;
    }

    Metadata createMetadata(WebClientServiceRequest request, WebClientServiceResponse response) {
        String name;
        if (response == null) {
            name = createName(request);
        } else {
            name = createName(request, response);
        }
        Metadata.Builder builder = Metadata.builder().withName(name);
        if (description != null) {
            builder = builder.withDescription(description);
        }
        return builder.build();
    }

    String createName(WebClientServiceRequest request, WebClientServiceResponse response) {
        return String.format(nameFormat(), request.method().text(), request.uri().host(), response.status().code());
    }

    String createName(WebClientServiceRequest request) {
        return String.format(nameFormat(), request.method().text(), request.uri().host());
    }

    boolean handlesMethod(Method method) {
        return methods().isEmpty() || methods().contains(method.text());
    }

    /**
     * Client metric builder.
     */
    public static final class Builder implements io.helidon.common.Builder<Builder, WebClientMetric> {

        private final WebClientMetricType type;
        private Set<String> methods = Collections.emptySet();
        private String nameFormat;
        private String description;
        private boolean success = true;
        private boolean errors = true;

        private Builder(WebClientMetricType type) {
            this.type = type;
        }

        /**
         * Adds metric supported methods.
         *
         * @param methods metric supported methods
         * @return updated builder instance
         */
        public Builder methods(String... methods) {
            this.methods = Arrays.stream(methods).map(String::toUpperCase).collect(Collectors.toSet());
            return this;
        }

        /**
         * Adds metric supported methods.
         *
         * @param methods metric supported methods
         * @return updated builder instance
         */
        public Builder methods(Method... methods) {
            this.methods = Arrays.stream(methods)
                                 .map(Method::text)
                                 .map(String::toUpperCase)
                                 .collect(Collectors.toSet());
            return this;
        }

        /**
         * Adds metric supported methods.
         *
         * @param methods metric supported methods
         * @return updated builder instance
         */
        public Builder methods(Collection<String> methods) {
            this.methods = methods.stream()
                                  .map(String::toUpperCase)
                                  .collect(Collectors.toSet());
            return this;
        }

        /**
         * Sets name format of the metric.
         *
         * @param nameFormat format of the metric
         * @return updated builder instance
         */
        public Builder nameFormat(String nameFormat) {
            this.nameFormat = nameFormat;
            return this;
        }

        /**
         * Sets the description of the metric.
         *
         * @param description metric description
         * @return updated builder instance
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets value if this metric should cover successful requests.
         *
         * @param success handle success
         * @return updated builder instance
         */
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Sets value if this metric should cover unsuccessful requests.
         *
         * @param errors handle errors
         * @return updated builder instance
         */
        public Builder errors(boolean errors) {
            this.errors = errors;
            return this;
        }

        /**
         * Configure a metric from configuration.
         * The following configuration key are used:
         * <table>
         * <caption>Client Metric configuration options</caption>
         * <tr>
         *     <th>key</th>
         *     <th>default</th>
         *     <th>description</th>
         * </tr>
         * <tr>
         *     <td>errors</td>
         *     <td>{@code true}</td>
         *     <td>Whether this metric triggers for error states</td>
         * </tr>
         * <tr>
         *     <td>success</td>
         *     <td>{@code true}</td>
         *     <td>Whether this metric triggers for successful executions</td>
         * </tr>
         * <tr>
         *     <td>name-format</td>
         *     <td>{@code client.success.method.hostname.status}</td>
         *     <td>A string format used to construct a metric name. The format gets three parameters: the method name,
         *     the hostname and the response code (if applicable)</td>
         * </tr>
         * <tr>
         *     <td>description</td>
         *     <td>&nbsp;</td>
         *     <td>Description of this metric.</td>
         * </tr>
         * </table>
         *
         * @param config configuration to configure this metric
         * @return updated builder instance
         */
        public Builder config(Config config) {
            config.get("methods").asList(String.class).ifPresent(this::methods);
            config.get("errors").asBoolean().ifPresent(this::errors);
            config.get("success").asBoolean().ifPresent(this::success);
            config.get("name-format").asString().ifPresent(this::nameFormat);
            config.get("description").asString().ifPresent(this::description);
            return this;
        }

        @Override
        public WebClientMetric build() {
            return type.createInstance(this);
        }
    }

    record Metadata(String name, String description) {

        static Builder builder() {
            return new Builder();
        }

        static class Builder {
            private String name;
            private String description;

            Builder withName(String name) {
                this.name = name;
                return this;
            }

            Builder withDescription(String description) {
                this.description = description;
                return this;
            }

            Metadata build() {
                return new Metadata(name, description);
            }

        }
    }
}
