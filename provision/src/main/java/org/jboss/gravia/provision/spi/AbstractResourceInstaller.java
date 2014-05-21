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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.ResourceUtils;

/**
 * An abstract {@link ResourceInstaller}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 13-Jan-2014
 */
public abstract class AbstractResourceInstaller implements ResourceInstaller {

    public abstract RuntimeEnvironment getEnvironment();

    @Override
    public ResourceHandle installResource(Context context, Resource resource) throws ProvisionException {
        IllegalArgumentAssertion.assertNotNull(resource, "resource");
        if (context == null) {
            context = new DefaultInstallerContext(resource);
        }
        try {
            return installResourceProtected(context, resource);
        } catch (RuntimeException rte) {
            throw rte;
        } catch (ProvisionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProvisionException("Cannot provision resource: " + resource, ex);
        }
    }

    protected abstract ResourceHandle installResourceProtected(Context context, Resource resource) throws Exception;

    /**
     * Get the list of relevant content capabilities
     */
    protected List<ContentCapability> getRelevantContentCapabilities(Resource resource) {
        List<ContentCapability> ccaps = new ArrayList<>();
        for (Capability cap : resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE)) {
            String includedTypes = cap.getDirective(ContentNamespace.CAPABILITY_INCLUDE_RUNTIME_TYPE_DIRECTIVE);
            String excludedTypes = cap.getDirective(ContentNamespace.CAPABILITY_EXCLUDE_RUNTIME_TYPE_DIRECTIVE);
            if (includedTypes == null && excludedTypes == null) {
                ccaps.add(cap.adapt(ContentCapability.class));
            } else {
                Set<RuntimeType> types = new HashSet<>();
                if (includedTypes == null) {
                    types.add(RuntimeType.getRuntimeType());
                }

                // Add all included runtime types
                types.addAll(getRuntimeTypes(includedTypes));

                // Remove all excluded runtime types
                types.removeAll(getRuntimeTypes(excludedTypes));

                // Content is relevant when the current runtime type is included
                if (types.contains(RuntimeType.getRuntimeType())) {
                    ccaps.add(cap.adapt(ContentCapability.class));
                }
            }
        }
        return ccaps;
    }

    private Set<RuntimeType> getRuntimeTypes(String directive) {
        Set<RuntimeType> types = new HashSet<>();
        if (directive != null) {
            for (String typespec : directive.split(",")) {
                types.add(RuntimeType.valueOf(typespec.toUpperCase()));
            }
        }
        return types;
    }

    protected ResourceContent getFirstRelevantResourceContent(Resource resource) {
        for (ContentCapability ccap : getRelevantContentCapabilities(resource)) {
            InputStream contentStream = ccap.getContentStream();
            if (contentStream == null) {
                URL contentURL = ccap.getContentURL();
                if (contentURL != null) {
                    try {
                        contentStream = contentURL.openStream();
                    } catch (IOException ex) {
                        throw new IllegalStateException("Cannot access content URL: " + contentURL, ex);
                    }
                }
            }
            if (contentStream != null) {
                final InputStream inputStream = contentStream;
                return new ResourceContent() {
                    @Override
                    public InputStream getContent() {
                        return inputStream;
                    }
                };
            }
        }
        throw new IllegalStateException("Cannot obtain content from: " + resource);
    }

    protected String getRuntimeName(Resource resource) {

        // #1 Try to get the runtime name from the identity capability
        Capability icap = resource.getIdentityCapability();
        String runtimeName = (String) icap.getAttribute(ContentNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE);

        // #2 Try to get the runtime name from the content cpability
        if (runtimeName == null) {
            List<ContentCapability> ccaps = getRelevantContentCapabilities(resource);
            if (ccaps.size() == 1) {
                ContentCapability ccap = ccaps.get(0);
                runtimeName = (String) ccap.getAttribute(ContentNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE);
            }
        }

        // #3 Use fallback name deriven from the resource identity
        if (runtimeName == null) {
            ResourceIdentity resid = resource.getIdentity();
            String qualifiedName = resid.getSymbolicName() + "-" + resid.getVersion();
            if (ResourceUtils.isShared(resource)) {
                runtimeName = qualifiedName + ".jar";
            } else if (RuntimeType.TOMCAT == RuntimeType.getRuntimeType()) {
                runtimeName = qualifiedName + ".war";
            } else {
                runtimeName = qualifiedName + ".jar";
            }
        }
        return runtimeName;
    }
}
