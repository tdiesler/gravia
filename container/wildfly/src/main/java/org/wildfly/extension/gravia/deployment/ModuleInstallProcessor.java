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
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.jboss.modules.ModuleClassLoader;
import org.wildfly.extension.gravia.GraviaConstants;

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

        // Get the headers from the manifest
        Dictionary<String, String> headers = null;
        ResourceRoot deploymentRoot = depUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        Manifest manifest = deploymentRoot != null ? deploymentRoot.getAttachment(Attachments.MANIFEST) : null;
        if (manifest != null) {
            headers = new ManifestHeadersProvider(manifest).getHeaders();
        }

        // Install the module
        ModuleClassLoader classLoader = depUnit.getAttachment(Attachments.MODULE).getClassLoader();
        try {
            Runtime runtime = RuntimeLocator.getRuntime();
            Module module = runtime.installModule(classLoader, resource, headers);
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
