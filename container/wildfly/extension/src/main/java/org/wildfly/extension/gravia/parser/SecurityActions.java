/*
 * #%L
 * Gravia :: Container :: WildFly :: Extension
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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


package org.wildfly.extension.gravia.parser;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged actions used by this package.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 19-Jan-2010
 */
class SecurityActions {

    // Hide ctor
    private SecurityActions() {
    }

    static ClassLoader getClassLoader(final Class<?> clazz) {
        return System.getSecurityManager() == null ? clazz.getClassLoader() : AccessController.doPrivileged(new GetClassLoaderAction(clazz));
    }

    static final class GetClassLoaderAction implements PrivilegedAction<ClassLoader> {
        private final Class<?> clazz;

        GetClassLoaderAction(final Class<?> clazz) {
            this.clazz = clazz;
        }

        public ClassLoader run() {
            return clazz.getClassLoader();
        }
    }
}
