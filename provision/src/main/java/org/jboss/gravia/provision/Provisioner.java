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

import java.io.InputStream;
import java.util.Dictionary;
import java.util.Set;

import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;

/**
 * The {@link Provisioner}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public interface Provisioner {

    /**
     * Get the associated runtime environment.
     */
    Environment getEnvironment();

    /**
     * Get the associated resolver.
     */
    Resolver getResolver();

    /**
     * Get the associated repository.
     */
    Repository getRepository();

    /**
     * Get the associated resource installer.
     */
    ResourceInstaller getResourceInstaller();

    /**
     * Perform a no-impact analysis of whether a set of requirements
     * can be sattisfied in the current environment with the current repository content.
     */
    ProvisionResult findResources(Set<Requirement> reqs);

    /**
     * Provision the needed delta to sattisfy the given set of requirements.
     * This performs a no-impact analysis of whether the given set of requirements can be sattisfied first.
     */
    Set<ResourceHandle> provisionResources(Set<Requirement> reqs) throws ProvisionException;

    /**
     * Install a resource from the given input stream.
     *
     * @see Provisioner#installResource(Resource)
     */
    ResourceHandle installResource(String runtimeName, InputStream inputStream, Dictionary<String, String> headers) throws ProvisionException;

    /**
     * Install a resource to the shared location from the given input stream.
     *
     * @see Provisioner#installSharedResource(Resource)
     */
    ResourceHandle installSharedResource(String runtimeName, InputStream inputStream, Dictionary<String, String> headers) throws ProvisionException;

    /**
     * Install the given resource.
     *
     * The resource must contain a {@link ContentCapability}.
     *
     * The caller must be aware of the environment content and take on resposibility that all
     * dependencies of the installed resource are sattisfied. The {@link Environment}, {@link Resolver} and {@link Repository}
     * are not used. Instead, it delegates directly to the container specific {@link ResourceInstaller}
     */
    ResourceHandle installResource(Resource resource, Dictionary<String, String> headers) throws ProvisionException;

    /**
     * Install the given resource to the shared location.
     *
     * @see Provisioner#installResource(Resource)
     */
    ResourceHandle installSharedResource(Resource resource, Dictionary<String, String> headers) throws ProvisionException;
}
