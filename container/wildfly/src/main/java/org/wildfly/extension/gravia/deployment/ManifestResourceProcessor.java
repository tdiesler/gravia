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

import java.util.jar.Manifest;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.gravia.resource.ManifestResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Make the {@link Resource} available from manifest
 *
 * @author Thomas.Diesler@jboss.com
 * @since 08-Oct-2013
 */
public class ManifestResourceProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit depUnit = phaseContext.getDeploymentUnit();
        Resource resource = depUnit.getAttachment(GraviaConstants.RESOURCE_KEY);
        if (resource != null)
            return;

        ResourceRoot deploymentRoot = depUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        Manifest manifest = deploymentRoot != null ? deploymentRoot.getAttachment(Attachments.MANIFEST) : null;
        if (manifest == null)
            return;

        ManifestResourceBuilder builder = new ManifestResourceBuilder().load(manifest);
        if (builder.isValid()) {
            resource = builder.getResource();
            depUnit.putAttachment(GraviaConstants.RESOURCE_KEY, resource);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit depUnit) {
    }
}
