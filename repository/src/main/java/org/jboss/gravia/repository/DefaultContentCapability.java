/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

package org.jboss.gravia.repository;

import java.util.Map;

import org.jboss.gravia.repository.spi.AbstractContentCapability;
import org.jboss.gravia.resource.spi.AbstractResource;

/**
 * The default implementation of a {@link ContentCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 05-Jul-2012
 */
public class DefaultContentCapability extends AbstractContentCapability {

    public DefaultContentCapability(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        super(resource, namespace, atts, dirs);
    }
}
