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

package org.jboss.gravia.provision.spi;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.ResourceAssociation;

/**
 * An abstract {@link ResourceInstaller}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 13-Jan-2014
 */
public abstract class AbstractResourceInstaller implements ResourceInstaller {

    public abstract RuntimeEnvironment getEnvironment();

    @Override
    public synchronized Set<ResourceHandle> installResources(List<Resource> resources, Map<Requirement, Resource> mapping) throws ProvisionException {
        Set<ResourceHandle> handles = new HashSet<ResourceHandle>();
        for (Resource res : resources) {
            ResourceIdentity identity = res.getIdentity();
            if (!isAbstract(res) && getEnvironment().getResource(identity) == null) {
                handles.add(installResourceInternal(res, mapping));
            }
        }
        return Collections.unmodifiableSet(handles);
    }

    @Override
    public synchronized ResourceHandle installResource(Resource res, Map<Requirement, Resource> mapping) throws ProvisionException {
        return installResourceInternal(res, mapping);
    }

    private synchronized ResourceHandle installResourceInternal(Resource resource, Map<Requirement, Resource> mapping) throws ProvisionException {
        ResourceAssociation.putResource(resource);
        try {
            if (isShared(resource)) {
                return installSharedResource(resource, mapping);
            } else {
                return installUnsharedResource(resource, mapping);
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (ProvisionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProvisionException("Cannot provision resource: " + resource, ex);
        } finally {
            ResourceAssociation.removeResource(resource.getIdentity());
        }
    }

    public abstract ResourceHandle installSharedResource(Resource resource, Map<Requirement, Resource> mapping) throws Exception;

    public abstract ResourceHandle installUnsharedResource(Resource resource, Map<Requirement, Resource> mapping) throws Exception;

    private boolean isAbstract(Resource res) {
        Object attval = res.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE);
        return IdentityNamespace.TYPE_ABSTRACT.equals(attval);
    }

    private boolean isShared(Resource resource) {
        Object attval = resource.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_SHARED_ATTRIBUTE);
        return Boolean.parseBoolean((String) attval);
    }
}
