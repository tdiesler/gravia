/*
 * #%L
 * Wildfly Gravia Subsystem
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
