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

import java.net.URL;
import java.util.List;

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * An abstract {@link ResourceInstaller}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 13-Jan-2014
 */
public abstract class AbstractResourceInstaller implements ResourceInstaller {

    public abstract RuntimeEnvironment getEnvironment();

    @Override
    public ResourceHandle installResource(Context context, Resource res) throws ProvisionException {
        return installResourceInternal(context, getRuntimeName(res), res, isShared(res));
    }

    @Override
    public ResourceHandle installSharedResource(Context context, Resource res) throws ProvisionException {
        return installResourceInternal(context, getRuntimeName(res), res, true);
    }

    private String getRuntimeName(Resource res) {
        Capability icap = res.getIdentityCapability();
        String runtimeName = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE);
        if (runtimeName == null) {
            String mvnatt = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE);
            if (mvnatt != null) {
                MavenCoordinates mavenid = MavenCoordinates.parse(mvnatt);
                runtimeName = mavenid.getArtifactId() + "-" + mavenid.getVersion() + "." + mavenid.getType();
            }
        }
        if (runtimeName == null) {
            List<Capability> ccaps = res.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
            IllegalArgumentAssertion.assertFalse(ccaps.isEmpty(), "Resource has not content capability");
            URL contentURL = (URL) ccaps.get(0).getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
            runtimeName = contentURL != null ? contentURL.getFile() : null;
        }
        IllegalArgumentAssertion.assertTrue(runtimeName != null, "Cannot obtain runtime name for: " + res);
        return runtimeName;
    }

    private synchronized ResourceHandle installResourceInternal(Context context, String runtimeName, Resource resource, boolean shared) throws ProvisionException {
        IllegalArgumentAssertion.assertNotNull(runtimeName, "runtimeName");
        IllegalArgumentAssertion.assertNotNull(resource, "resource");
        if (context == null) {
            context = new DefaultInstallerContext(resource);
        }
        try {
            return installResourceProtected(context, runtimeName, resource, shared);
        } catch (RuntimeException rte) {
            throw rte;
        } catch (ProvisionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProvisionException("Cannot provision resource: " + resource, ex);
        }
    }

    protected abstract ResourceHandle installResourceProtected(Context context, String runtimeName, Resource resource, boolean shared) throws Exception;

    private boolean isShared(Resource resource) {
        Object attval = resource.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_SHARED_ATTRIBUTE);
        return Boolean.parseBoolean((String) attval);
    }
}
