/*
 * #%L
 * JBossOSGi Resolver API
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
package org.jboss.gravia.resource.spi;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Wire;

/**
 * An abstract {@link Wire}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractWire implements Wire {

    private final Capability capability;
    private final Requirement requirement;
    
    public AbstractWire(Capability capability, Requirement requirement) {
        if (capability == null)
            throw new IllegalArgumentException("Null capability");
        if (requirement == null)
            throw new IllegalArgumentException("Null requirement");
        this.capability = capability;
        this.requirement = requirement;
    }

    @Override
    public Capability getCapability() {
        return capability;
    }

    @Override
    public Requirement getRequirement() {
        return requirement;
    }

    @Override
    public Resource getProvider() {
        return capability.getResource();
    }

    @Override
    public Resource getRequirer() {
        return requirement.getResource();
    }

    @Override
    public String toString() {
        return requirement + " => " + capability;
    }

}
