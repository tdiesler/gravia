/*
 * #%L
 * JBossOSGi Runtime
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.gravia.runtime.spi;

/**
 * A provider for Runtime properties.
 *
 * @see RuntimeFactory#createRuntime(PropertiesProvider)
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface PropertiesProvider {

    /**
     * Returns the value of the specified property. If the key is not found in
     * the Runtime properties, the system properties are then searched. The
     * method returns {@code null} if the property is not found.
     *
     * @param key The name of the requested property.
     * @return The value of the requested property, or {@code null} if the
     *         property is undefined.
     */
    Object getProperty(String key);

    /**
     * Returns the value of the specified property. If the key is not found in
     * the Runtime properties, the system properties are then searched. The
     * method returns provided default value if the property is not found.
     *
     * @param key The name of the requested property.
     * @return The value of the requested property, or the provided default value if the
     *         property is undefined.
     */
    Object getProperty(String key, Object defaultValue);
}
