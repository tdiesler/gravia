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

import java.util.Map;

import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.RequirementBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;

/**
 * An abstract {@link RequirementBuilder}.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractRequirementBuilder implements RequirementBuilder {

    private final ResourceBuilder resbuilder;
    private final Requirement requirement;

    public AbstractRequirementBuilder(String namespace) {
        this(namespace, null);
    }

    public AbstractRequirementBuilder(String namespace, String nsvalue) {
        resbuilder = createResourceBuilder();
        resbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "anonymous");
        requirement = resbuilder.addRequirement(namespace, nsvalue);
    }

    protected abstract ResourceBuilder createResourceBuilder();

    @Override
    public Map<String, Object> getAttributes() {
        return requirement.getAttributes();
    }

    @Override
    public Map<String, String> getDirectives() {
        return requirement.getDirectives();
    }

    @Override
    public Requirement getRequirement() {
        Resource resource = resbuilder.getResource();
        String namespace = requirement.getNamespace();
        return resource.getRequirements(namespace).get(0);
    }

}
