/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.gravia.runtime;

import org.jboss.gravia.Constants;
import org.jboss.gravia.utils.NotNullException;

/**
 * The enumeration of supported target containers
 *
 * @author thomas.diesler@jbos.com
 * @since 22-Nov-2013
 */
public enum RuntimeType {

    KARAF, TOMCAT, WILDFLY, OTHER;

    public static RuntimeType getRuntimeType() {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        return RuntimeType.getRuntimeType(runtime);
    }

    public static RuntimeType getRuntimeType(Runtime runtime) {
        NotNullException.assertValue(runtime, "runtime");
        Object type = runtime.getProperty(Constants.RUNTIME_TYPE);
        return RuntimeType.getRuntimeType((String) type);
    }

    public static RuntimeType getRuntimeType(String type) {
        String upper = type != null ? type.toUpperCase() : null;
        try {
            return RuntimeType.valueOf(upper);
        } catch (RuntimeException ex) {
            return OTHER;
        }
    }
}