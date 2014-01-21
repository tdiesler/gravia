/*
 * #%L
 * Gravia :: Container :: Tomcat :: Webapp
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
package org.jboss.gravia.container.tomcat.webapp;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.gravia.container.tomcat.support.TomcatPropertiesProvider;
import org.jboss.gravia.container.tomcat.support.TomcatResourceInstaller;
import org.jboss.gravia.container.tomcat.support.TomcatRuntimeFactory;
import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.repository.DefaultRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration.Registration;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.embedded.spi.BundleContextAdaptor;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimePropertiesProvider;
import org.osgi.framework.BundleContext;

/**
 * Activates the {@link Runtime} as part of the web app lifecycle.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Nov-2013
 */
@WebListener
public class GraviaTomcatActivator implements ServletContextListener {

    private Registration repositoryRegistration;
    private ServiceRegistration<Provisioner> provisionerRegistration;
    private ServiceRegistration<Resolver> resolverRegistration;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        // Create the runtime
        ServletContext servletContext = event.getServletContext();
        PropertiesProvider propsProvider = new TomcatPropertiesProvider(servletContext);
        Runtime runtime = RuntimeLocator.createRuntime(new TomcatRuntimeFactory(servletContext), propsProvider);
        runtime.init();

        // HttpService integration
        ModuleContext moduleContext = runtime.getModuleContext();
        BundleContext bundleContext = new BundleContextAdaptor(moduleContext);
        servletContext.setAttribute(BundleContext.class.getName(), bundleContext);

        // Register the {@link Repository}, {@link Resolver}, {@link Provisioner} services
        Repository repository = registerRepositoryService(runtime);
        Resolver resolver = registerResolverService(runtime);
        registerProvisionerService(servletContext, runtime, repository, resolver);
    }

    private Provisioner registerProvisionerService(ServletContext servletContext, Runtime runtime, Repository repository, Resolver resolver) {
        RuntimeEnvironment environment = createEnvironment(servletContext, runtime);
        TomcatResourceInstaller installer = new TomcatResourceInstaller(environment);
        Provisioner provisioner = new DefaultProvisioner(environment, resolver, repository, installer);
        ModuleContext syscontext = runtime.getModuleContext();
        provisionerRegistration = syscontext.registerService(Provisioner.class, provisioner, null);
        return provisioner;
    }

    private RuntimeEnvironment createEnvironment(ServletContext servletContext, Runtime runtime) {
        return new RuntimeEnvironment(runtime).initDefaultContent();
    }

    private Resolver registerResolverService(Runtime runtime) {
        Resolver resolver = new DefaultResolver();
        ModuleContext syscontext = runtime.getModuleContext();
        resolverRegistration = syscontext.registerService(Resolver.class, resolver, null);
        return resolver;
    }

    private Repository registerRepositoryService(final Runtime runtime) {
        PropertiesProvider propertyProvider = new RuntimePropertiesProvider(runtime);
        Repository repository = new DefaultRepository(propertyProvider);
        ModuleContext syscontext = runtime.getModuleContext();
        repositoryRegistration =  RepositoryRuntimeRegistration.registerRepository(syscontext, repository);
        return repository;
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (provisionerRegistration != null)
            provisionerRegistration.unregister();
        if (repositoryRegistration != null)
            repositoryRegistration.unregister();
        if (resolverRegistration != null)
            resolverRegistration.unregister();
    }
}
