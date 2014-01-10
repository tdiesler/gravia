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

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Add default system dependencies to a Gravia deployment.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 10-Jan-2014
 */
public class ModuleDependenciesProcessor implements DeploymentUnitProcessor {

    private static final List<ModuleDependency> systemDependencies = new ArrayList<ModuleDependency>();
    static {
        systemDependencies.add(new ModuleDependency(Module.getBootModuleLoader(), ModuleIdentifier.create("org.osgi.core"), false, false, false, false));
        systemDependencies.add(new ModuleDependency(Module.getBootModuleLoader(), ModuleIdentifier.create("org.osgi.enterprise"), false, false, false, false));
        systemDependencies.add(new ModuleDependency(Module.getBootModuleLoader(), ModuleIdentifier.create("org.jboss.gravia"), false, false, false, false));
    }

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit depUnit = phaseContext.getDeploymentUnit();
        if (depUnit.hasAttachment(GraviaConstants.RESOURCE_KEY)) {
            ModuleSpecification moduleSpecification = depUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
            moduleSpecification.addSystemDependencies(systemDependencies);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit depUnit) {
    }
}
