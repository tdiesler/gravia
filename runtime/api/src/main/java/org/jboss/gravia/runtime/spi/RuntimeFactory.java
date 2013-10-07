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

import org.jboss.gravia.runtime.Runtime;


/**
 * A factory for the Runtime.
 *
 * <p>
 * An implementation is provided for each supported target container.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface RuntimeFactory {

    Runtime createRuntime(PropertiesProvider propertiesProvider);
}