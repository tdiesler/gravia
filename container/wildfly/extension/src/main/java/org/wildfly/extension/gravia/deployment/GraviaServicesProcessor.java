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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.gravia.resource.Resource;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Allways make some gravia integration services available
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-Jun-2013
 */
public class GraviaServicesProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit depUnit = phaseContext.getDeploymentUnit();
        Resource resource = depUnit.getAttachment(GraviaConstants.RESOURCE_KEY);
        if (resource != null) {
            phaseContext.addDeploymentDependency(GraviaConstants.RUNTIME_SERVICE_NAME, GraviaConstants.RUNTIME_KEY);
            phaseContext.addDeploymentDependency(GraviaConstants.REPOSITORY_SERVICE_NAME, GraviaConstants.REPOSITORY_KEY);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit depUnit) {
    }
}
