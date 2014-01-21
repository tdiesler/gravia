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

import java.io.File;
import java.io.IOException;

import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.gravia.Constants;
import org.jboss.gravia.repository.DefaultRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration.Registration;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service responsible for preloading the {@link Repository}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 27-Jun-2013
 */
public class RepositoryService extends AbstractService<Repository> {

    private final InjectedValue<ServerEnvironment> injectedServerEnvironment = new InjectedValue<ServerEnvironment>();
    private final InjectedValue<ModuleContext> injectedModuleContext = new InjectedValue<ModuleContext>();
    private Registration registration;
    private Repository repository;

    public ServiceController<Repository> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<Repository> builder = serviceTarget.addService(GraviaConstants.REPOSITORY_SERVICE_NAME, this);
        builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, injectedServerEnvironment);
        builder.addDependency(GraviaConstants.MODULE_CONTEXT_SERVICE_NAME, ModuleContext.class, injectedModuleContext);
        builder.addListener(verificationHandler);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {

        // Create the {@link ConfigurationPropertyProvider}
        PropertiesProvider propertyProvider = new DefaultPropertiesProvider() {
            @Override
            public Object getProperty(String key, Object defaultValue) {
                Object value = super.getProperty(key, defaultValue);
                if (value == null && Constants.PROPERTY_REPOSITORY_STORAGE_DIR.equals(key)) {
                    try {
                        ServerEnvironment serverenv = injectedServerEnvironment.getValue();
                        File storageDir = new File(serverenv.getServerDataDir().getPath() + File.separator + "repository");
                        value = storageDir.getCanonicalPath();
                    } catch (IOException ex) {
                        throw new IllegalStateException("Cannot create repository storage area");
                    }
                }
                return value != null ? value : defaultValue;
            }
        };
        repository = new DefaultRepository(propertyProvider);

        // Register the repository as a service
        ModuleContext syscontext = injectedModuleContext.getValue();
        registration =  RepositoryRuntimeRegistration.registerRepository(syscontext, repository);
    }

    @Override
    public void stop(StopContext context) {
        if (registration != null) {
            registration.unregister();
        }
    }

    @Override
    public Repository getValue() throws IllegalStateException {
        return repository;
    }
}
