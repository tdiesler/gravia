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
