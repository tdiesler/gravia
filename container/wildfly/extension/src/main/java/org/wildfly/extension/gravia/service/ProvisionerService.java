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


package org.wildfly.extension.gravia.service;

import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service providing the {@link Provisioner}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 27-Jun-2013
 */
public class ProvisionerService extends AbstractService<Provisioner> {

    static final Logger LOGGER = LoggerFactory.getLogger(GraviaConstants.class.getPackage().getName());

    private final InjectedValue<ModuleContext> injectedModuleContext = new InjectedValue<ModuleContext>();
    private final InjectedValue<Environment> injectedEnvironment = new InjectedValue<Environment>();
    private final InjectedValue<Repository> injectedRepository = new InjectedValue<Repository>();
    private final InjectedValue<Resolver> injectedResolver = new InjectedValue<Resolver>();
    private final InjectedValue<ResourceInstaller> injectedInstaller = new InjectedValue<ResourceInstaller>();
    private ServiceRegistration<Provisioner> registration;
    private Provisioner provisioner;

    public ServiceController<Provisioner> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<Provisioner> builder = serviceTarget.addService(GraviaConstants.PROVISIONER_SERVICE_NAME, this);
        builder.addDependency(GraviaConstants.ENVIRONMENT_SERVICE_NAME, Environment.class, injectedEnvironment);
        builder.addDependency(GraviaConstants.MODULE_CONTEXT_SERVICE_NAME, ModuleContext.class, injectedModuleContext);
        builder.addDependency(GraviaConstants.REPOSITORY_SERVICE_NAME, Repository.class, injectedRepository);
        builder.addDependency(GraviaConstants.RESOLVER_SERVICE_NAME, Resolver.class, injectedResolver);
        builder.addDependency(GraviaConstants.RESOURCE_INSTALLER_SERVICE_NAME, ResourceInstaller.class, injectedInstaller);
        builder.addListener(verificationHandler);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {

        Resolver resolver = injectedResolver.getValue();
        Repository repository = injectedRepository.getValue();
        Environment environment = injectedEnvironment.getValue();
        ResourceInstaller installer = injectedInstaller.getValue();
        provisioner = new DefaultProvisioner(environment, resolver, repository, installer);

        // Register the provisioner as a service
        ModuleContext syscontext = injectedModuleContext.getValue();
        registration = syscontext.registerService(Provisioner.class, provisioner, null);
    }

    @Override
    public void stop(StopContext context) {
        if (registration != null) {
            registration.unregister();
        }
    }

    @Override
    public Provisioner getValue() throws IllegalStateException {
        return provisioner;
    }
}
