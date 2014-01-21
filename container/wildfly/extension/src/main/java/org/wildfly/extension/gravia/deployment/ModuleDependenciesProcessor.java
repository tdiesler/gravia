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
