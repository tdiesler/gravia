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


package org.wildfly.extension.gravia.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.gravia.Constants;
import org.jboss.gravia.repository.DefaultMavenDelegateRepository;
import org.jboss.gravia.repository.DefaultRepositoryStorage;
import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryBuilder;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.Resource;
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

    private final InjectedValue<ModuleContext> injectedModuleContext = new InjectedValue<ModuleContext>();
    private final InjectedValue<ServerEnvironment> injectedServerEnvironment = new InjectedValue<ServerEnvironment>();
    private ServiceRegistration<Repository> registration;
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
        RepositoryBuilder builder = new RepositoryBuilder(propertyProvider);
        builder.setRepositoryDelegate(new DefaultMavenDelegateRepository(propertyProvider));
        builder.setRepositoryStorage(new DefaultRepositoryStorage(propertyProvider));
        repository = builder.getRepository();

        // Install gravia features to the repository
        ModuleClassLoader classLoader = Module.getCallerModule().getClassLoader();
        Iterator<Resource> itres = classLoader.iterateResources("META-INF/repository-content", false);
        while(itres.hasNext()) {
            Resource res = itres.next();
            try {
                InputStream input = res.openStream();
                RepositoryReader reader = new DefaultRepositoryXMLReader(input);
                org.jboss.gravia.resource.Resource auxres = reader.nextResource();
                while (auxres != null) {
                    RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
                    if (storage.getResource(auxres.getIdentity()) == null) {
                        storage.addResource(auxres);
                    }
                    auxres = reader.nextResource();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot install feature to repository: " + res.getName());
            }
        }

        // Register the repository as a service
        ModuleContext syscontext = injectedModuleContext.getValue();
        registration = syscontext.registerService(Repository.class, repository, null);
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
