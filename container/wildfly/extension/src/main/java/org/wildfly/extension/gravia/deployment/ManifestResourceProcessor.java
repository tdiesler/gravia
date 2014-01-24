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

import java.util.jar.Manifest;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.gravia.resource.ManifestResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.spi.NamedResourceAssociation;
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

        resource = NamedResourceAssociation.getResource(depUnit.getName());
        if (resource == null) {
            ResourceRoot deploymentRoot = depUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
            Manifest manifest = deploymentRoot != null ? deploymentRoot.getAttachment(Attachments.MANIFEST) : null;
            if (manifest != null) {
                ManifestResourceBuilder builder = new ManifestResourceBuilder().load(manifest);
                if (builder.isValid()) {
                    resource = builder.getResource();
                }
            }
        }

        if (resource != null) {
            depUnit.putAttachment(GraviaConstants.RESOURCE_KEY, resource);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit depUnit) {
    }
}
