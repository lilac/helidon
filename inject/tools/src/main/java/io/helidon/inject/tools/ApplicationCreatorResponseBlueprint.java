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

package io.helidon.inject.tools;

import io.helidon.builder.api.Prototype;

/**
 * Response from {@link io.helidon.inject.tools.spi.ApplicationCreator}.
 *
 * @see io.helidon.inject.tools.spi.ApplicationCreator
 */
@Prototype.Blueprint
public interface ApplicationCreatorResponseBlueprint extends GeneralCreatorResponseBlueprint {

    /**
     * The basic description for the {@link io.helidon.inject.api.Application} generated.
     *
     * @return describes the application generated (package and class)
     */
    ApplicationCreatorCodeGen applicationCodeGen();

}
