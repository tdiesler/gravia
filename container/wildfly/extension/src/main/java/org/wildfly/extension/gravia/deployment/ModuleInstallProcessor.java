/*
 * #%L
 * Gravia :: Container :: WildFly :: Extension
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


package org.wildfly.extension.gravia.deployment;

import java.util.Dictionary;
import java.util.jar.Manifest;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.spi.AttachableSupport;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.ResourceAssociation;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.jboss.modules.ModuleClassLoader;
import org.wildfly.extension.gravia.GraviaConstants;
import org.wildfly.extension.gravia.service.WildflyRuntime;

/**
 * Install/Uninstall the {@link Module} from the {@link Runtime}
 *
 * @author Thomas.Diesler@jboss.com
 * @since 08-Oct-2013
 */
public class ModuleInstallProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit depUnit = phaseContext.getDeploymentUnit();
        Resource resource = depUnit.getAttachment(GraviaConstants.RESOURCE_KEY);
        if (resource == null)
            return;

        // Use the associated resource if we have one
        ResourceIdentity identity = resource.getIdentity();
        Resource association = ResourceAssociation.getResource(identity);
        if (association != null) {
            depUnit.putAttachment(GraviaConstants.RESOURCE_KEY, association);
            resource = association;
        }

        // Get the headers from the manifest
        Dictionary<String, String> headers = null;
        ResourceRoot deploymentRoot = depUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        Manifest manifest = deploymentRoot != null ? deploymentRoot.getAttachment(Attachments.MANIFEST) : null;
        if (manifest != null) {
            headers = new ManifestHeadersProvider(manifest).getHeaders();
        }

        // Initialize the module install context
        AttachableSupport context = new AttachableSupport();
        context.putAttachment(WildflyRuntime.DEPLOYMENT_ROOT_KEY, deploymentRoot);

        // Install the module
        ModuleClassLoader classLoader = depUnit.getAttachment(Attachments.MODULE).getClassLoader();
        try {
            Runtime runtime = RuntimeLocator.getRequiredRuntime();
            Module module = runtime.installModule(classLoader, resource, headers, context);
            depUnit.putAttachment(GraviaConstants.MODULE_KEY, module);
        } catch (ModuleException ex) {
            throw new DeploymentUnitProcessingException(ex);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit depUnit) {
        Module module = depUnit.getAttachment(GraviaConstants.MODULE_KEY);
        if (module != null && module.getState() != Module.State.UNINSTALLED) {
            module.uninstall();
        }
    }
}
