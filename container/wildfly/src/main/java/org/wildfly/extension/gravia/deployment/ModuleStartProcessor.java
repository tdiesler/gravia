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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Start/Stop the attached Gravia Module
 *
 * @author Thomas.Diesler@jboss.com
 * @since 08-Oct-2013
 */
public class ModuleStartProcessor implements DeploymentUnitProcessor {

    static final Logger LOGGER = LoggerFactory.getLogger(GraviaConstants.class.getPackage().getName());

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
