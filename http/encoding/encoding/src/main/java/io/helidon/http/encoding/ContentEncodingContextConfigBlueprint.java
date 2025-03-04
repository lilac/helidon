/*
 * Copyright (c) 2023 Oracle and/or its affiliates.
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

package io.helidon.http.encoding;

import java.util.List;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;
import io.helidon.config.metadata.Configured;
import io.helidon.config.metadata.ConfiguredOption;
import io.helidon.http.encoding.spi.ContentEncodingProvider;

@Prototype.Blueprint
@Configured
interface ContentEncodingContextConfigBlueprint extends Prototype.Factory<ContentEncodingContext> {
    /**
     * List of content encodings that should be used.
     * Encodings configured here have priority over encodings discovered through service loader.
     *
     * @return list of content encodings to be used (such as {@code gzip,deflate})
     */
    @Option.Singular
    @ConfiguredOption(provider = true,
                      providerType = ContentEncodingProvider.class)
    List<ContentEncoding> contentEncodings();
}
