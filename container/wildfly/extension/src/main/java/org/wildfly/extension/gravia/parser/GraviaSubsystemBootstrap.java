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

package org.wildfly.extension.gravia.parser;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
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
import org.wildfly.extension.gravia.service.ProvisionerService;
import org.wildfly.extension.gravia.service.RepositoryService;
import org.wildfly.extension.gravia.service.ResolverService;
import org.wildfly.extension.gravia.service.ResourceInstallerService;
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

    public List<ServiceController<?>> getSubsystemServices(OperationContext context, ServiceVerificationHandler verificationHandler) {
        List<ServiceController<?>> controllers = new ArrayList<ServiceController<?>>();
        controllers.add(getBoostrapService(context, verificationHandler));
        controllers.add(new EnvironmentService().install(context.getServiceTarget(), verificationHandler));
        controllers.add(new ModuleContextService().install(context.getServiceTarget(), verificationHandler));
        controllers.add(new ProvisionerService().install(context.getServiceTarget(), verificationHandler));
        controllers.add(new ResolverService().install(context.getServiceTarget(), verificationHandler));
        controllers.add(new ResourceInstallerService().install(context.getServiceTarget(), verificationHandler));
        controllers.add(new RepositoryService().install(context.getServiceTarget(), verificationHandler));
        controllers.add(getRuntimeService(context, verificationHandler));
        return controllers;
    }

    protected ServiceController<?> getBoostrapService(OperationContext context, ServiceVerificationHandler verificationHandler) {
        return new GraviaBootstrapService().install(context.getServiceTarget(), verificationHandler);
    }

    protected ServiceController<Runtime> getRuntimeService(OperationContext context, ServiceVerificationHandler verificationHandler) {
        return new RuntimeService().install(context.getServiceTarget(), verificationHandler);
    }

    public void addDeploymentUnitProcessors(DeploymentProcessorTarget processorTarget) {
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_GRAVIA_SERVICES_PROVIDER, new GraviaServicesProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_GRAVIA_RESOURCE, new ManifestResourceProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, DEPENDENCIES_GRAVIA_RESOURCE, new ModuleDependenciesProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, POST_MODULE_GRAVIA_MODULE_INSTALL, new ModuleInstallProcessor());
        processorTarget.addDeploymentProcessor(GraviaExtension.SUBSYSTEM_NAME, Phase.INSTALL, INSTALL_GRAVIA_MODULE_START, new ModuleStartProcessor());
    }
}
