/*
 * #%L
 * Gravia :: Resource
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
package org.jboss.gravia.resource;

import java.util.List;

/**
 * A resource is associated with Capabilities/Requirements.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Resource extends Adaptable, Attachable {

    String KEY_CAPABILITY = "capability";
    String KEY_REQUIREMENT = "requirement";
    String KEY_IDENTITY = "identity";

    ResourceIdentity getIdentity();

    Capability getIdentityCapability();

    List<Capability> getCapabilities(String namespace);

    List<Requirement> getRequirements(String namespace);
}
