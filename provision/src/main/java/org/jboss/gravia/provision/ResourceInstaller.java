/*
 * #%L
 * Gravia :: Provision
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.provision;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;

/**
 * The container specific {@link ResourceInstaller}.
 *
 * @author thomas.diesler@jboss.com
 * @since 07-Jan-2014
 */
public interface ResourceInstaller {

    Set<ResourceHandle> installResources(List<Resource> resources, Map<Requirement, Resource> mapping) throws ProvisionException;

    ResourceHandle installResource(Resource res, Map<Requirement, Resource> mapping) throws ProvisionException;

}
