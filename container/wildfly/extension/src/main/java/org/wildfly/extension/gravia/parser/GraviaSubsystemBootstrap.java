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

package org.wildfly.extension.gravia.parser;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.msc.service.ServiceController;
import org.wildfly.extension.gravia.deployment.GraviaServicesProcessor;
import org.wildfly.extension.gravia.deployment.ManifestResourceProcessor;
import org.wildfly.extension.gravia.deployment.ModuleDependenciesProcessor;
import org.wildfly.extension.gravia.deployment.ModuleInstallProcessor;
import org.wildfly.extension.gravia.deployment.ModuleStartProcessor;
import org.wildfly.extension.gravia.service.EnvironmentService;
import org.wildfly.extension.gravia.service.GraviaBootstrapService;
import org.wildfly.extension.gravia.service.ModuleContextService;
import org.wildfly.extension.gravia.service.WildFlyResourceInstaller;
import org.wildfly.extension.gravia.service.RuntimeService;

/**
 * Add the gravia subsystem services.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 19-Apr-2013
 */
public class GraviaSubsystemBootstrap {

    public static final int PARSE_GRAVIA_SERVICES_PROVIDER = Phase.PARSE_OSGI_SUBSYSTEM_ACTIVATOR + 0x01;
    public static final int PARSE_GRAVIA_RESOURCE = Phase.PARSE_OSGI_DEPLOYMENT + 0x01;
    public static final int DEPENDENCIES_GRAVIA_RESOURCE = Phase.DEPENDENCIES_BATCH + 0x01;
    public static final int POST_MODULE_GRAVIA_MODULE_INSTALL = Phase.POST_MODULE_REFLECTION_INDEX + 0x01;
    public static final int INSTALL_GRAVIA_MODULE_START = Phase.INSTALL_DEPLOYMENT_COMPLETE_SERVICE - 0x01;

    public List<ServiceController<?>> getSubsystemServices(OperationContext context) {
        List<ServiceController<?>> controllers = new ArrayList<ServiceController<?>>();
        controllers.add(getBoostrapService(context));
        controllers.add(getResourceInstallService(context));
        controllers.add(getRuntimeEnvironmentService(context));
        controllers.add(getRuntimeService(context));
        controllers.add(getSystemContextService(context));
        return controllers;
    }

    protected ServiceController<?> getBoostrapService(OperationContext context) {
        return new GraviaBootstrapService().install(context.getServiceTarget());
    }

    protected ServiceController<ResourceInstaller> getResourceInstallService(OperationContext context) {
        return new WildFlyResourceInstaller().install(context.getServiceTarget());
    }

    protected ServiceController<Environment> getRuntimeEnvironmentService(OperationContext context) {
        return new EnvironmentService().install(context.getServiceTarget());
    }

    protected ServiceController<Runtime> getRuntimeService(OperationContext context) {
        return new RuntimeService().install(context.getServiceTarget());
    }

    protected ServiceController<ModuleContext> getSystemContextService(OperationContext context) {
        return new ModuleContextService().install(context.getServiceTarget());
    }

    public void addDeploymentUnitProcessors(DeploymentProcessorTarget processorTarget) {
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_GRAVIA_SERVICES_PROVIDER, new GraviaServicesProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_GRAVIA_RESOURCE, new ManifestResourceProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, DEPENDENCIES_GRAVIA_RESOURCE, new ModuleDependenciesProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, POST_MODULE_GRAVIA_MODULE_INSTALL, new ModuleInstallProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.INSTALL, INSTALL_GRAVIA_MODULE_START, new ModuleStartProcessor());
    }
}
