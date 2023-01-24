/*
 * Copyright 2018 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.github.gestalt.config.cdi;

public class GestaltConfigException extends RuntimeException {
    private final String configPropertyName;

    public GestaltConfigException(String message) {
        super(message);
        this.configPropertyName = null;
    }

    public GestaltConfigException(String message, String configPropertyName) {
        super(message);
        this.configPropertyName = configPropertyName;
    }

    public GestaltConfigException(String message, Throwable cause) {
        super(message, cause);
        this.configPropertyName = null;
    }

    public GestaltConfigException(String message, String configPropertyName, Throwable cause) {
        super(message, cause);
        this.configPropertyName = configPropertyName;
    }

    public String getConfigPropertyName() {
        return configPropertyName;
    }

}
