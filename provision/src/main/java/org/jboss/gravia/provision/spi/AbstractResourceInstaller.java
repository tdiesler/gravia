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
import java.util.Set;

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.utils.NotNullException;

/**
 * An abstract {@link ResourceInstaller}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 13-Jan-2014
 */
public abstract class AbstractResourceInstaller implements ResourceInstaller {

    public abstract RuntimeEnvironment getEnvironment();

    @Override
    public synchronized Set<ResourceHandle> installResources(Context context) throws ProvisionException {
        NotNullException.assertValue(context, "context");
        Set<ResourceHandle> handles = new HashSet<ResourceHandle>();
        for (Resource res : context.getResources()) {
            ResourceIdentity identity = res.getIdentity();
            if (!isAbstract(res) && getEnvironment().getResource(identity) == null) {
                handles.add(installResourceInternal(context, res, isShared(res)));
            }
        }
        return Collections.unmodifiableSet(handles);
    }

    @Override
    public ResourceHandle installResource(Context context, Resource res) throws ProvisionException {
        return installResourceInternal(context, res, isShared(res));
    }

    @Override
    public ResourceHandle installSharedResource(Context context, Resource res) throws ProvisionException {
        return installResourceInternal(context, res, true);
    }

    private synchronized ResourceHandle installResourceInternal(Context context, Resource resource, boolean shared) throws ProvisionException {
        NotNullException.assertValue(resource, "resource");
        if (context == null) {
            context = new DefaultInstallerContext(resource);
        }
        try {
            return shared ? processSharedResource(context, resource) : processUnsharedResource(context, resource);
        } catch (RuntimeException rte) {
            throw rte;
        } catch (ProvisionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProvisionException("Cannot provision resource: " + resource, ex);
        }
    }

    public abstract ResourceHandle processSharedResource(Context context, Resource resource) throws Exception;

    public abstract ResourceHandle processUnsharedResource(Context context, Resource resource) throws Exception;

    private boolean isAbstract(Resource res) {
        Object attval = res.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE);
        return IdentityNamespace.TYPE_ABSTRACT.equals(attval);
    }

    private boolean isShared(Resource resource) {
        Object attval = resource.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_SHARED_ATTRIBUTE);
        return Boolean.parseBoolean((String) attval);
    }
}
