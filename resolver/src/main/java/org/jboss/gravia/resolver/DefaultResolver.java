/*
 * #%L
 * JBossOSGi Resolver Felix
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
package org.jboss.gravia.resolver;

import java.util.List;

import org.jboss.gravia.resolver.spi.AbstractResolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultWire;
import org.jboss.gravia.resource.DefaultWiring;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.spi.AbstractWire;
import org.jboss.gravia.resource.spi.AbstractWiring;

/**
 * The default {@link Resolver}.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class DefaultResolver extends AbstractResolver {

    @Override
    protected AbstractWire createWire(Requirement req, Capability cap) {
        return new DefaultWire(req, cap);
    }

    @Override
    protected AbstractWiring createWiring(Resource resource, List<Wire> reqwires, List<Wire> provwires) {
        return new DefaultWiring(resource, reqwires, provwires);
    }
    
}
