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

import static org.wildfly.extension.gravia.GraviaExtensionLogger.LOGGER;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Start/Stop the attached Gravia Module
 *
 * @author Thomas.Diesler@jboss.com
 * @since 08-Oct-2013
 */
public class ModuleStartProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit depUnit = phaseContext.getDeploymentUnit();
        Module module = depUnit.getAttachment(GraviaConstants.MODULE_KEY);
        if (module != null) {
            try {
                module.start();
            } catch (ModuleException ex) {
                throw new DeploymentUnitProcessingException(ex);
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit depUnit) {
        Module module = depUnit.getAttachment(GraviaConstants.MODULE_KEY);
        if (module != null && module.getState() != Module.State.UNINSTALLED) {
            try {
                module.stop();
            } catch (ModuleException ex) {
                LOGGER.error("Cannot stop module: " + module, ex);
            }
        }
    }
}
