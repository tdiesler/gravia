/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

}
