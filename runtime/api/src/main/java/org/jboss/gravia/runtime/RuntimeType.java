/*
 * #%L
 * JBossOSGi SPI
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
