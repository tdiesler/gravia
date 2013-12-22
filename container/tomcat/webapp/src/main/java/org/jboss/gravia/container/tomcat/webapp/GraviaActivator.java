/*
 * #%L
 * Gravia :: Integration Tests :: Common
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
package org.jboss.gravia.container.tomcat.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.gravia.Constants;
import org.jboss.gravia.container.tomcat.extension.TomcatRuntimeFactory;
import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.repository.DefaultMavenIdentityRepository;
import org.jboss.gravia.repository.DefaultPersistentRepository;
import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryAggregator;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.embedded.spi.BundleContextAdaptor;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
import org.osgi.framework.BundleContext;

/**
 * Activates the {@link Runtime} as part of the web app lifecycle.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Nov-2013
 */
@WebListener
public class GraviaActivator implements ServletContextListener {

    private final static File catalinaHome = new File(SecurityActions.getSystemProperty("catalina.home", null));
    private final static File catalinaWork = new File(catalinaHome.getPath() + File.separator + "work");

    private ServiceRegistration<Repository> repositoryRegistration;
    private ServiceRegistration<Provisioner> provisionerRegistration;
    private ServiceRegistration<Resolver> resolverRegistration;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        // Create the runtime
        ServletContext servletContext = event.getServletContext();
        Properties sysprops = getRuntimeProperties(servletContext);
        DefaultPropertiesProvider propsProvider = new DefaultPropertiesProvider(sysprops, true);
        Runtime runtime = RuntimeLocator.createRuntime(new TomcatRuntimeFactory(servletContext), propsProvider);
        runtime.init();

        // HttpService integration
        ModuleContext moduleContext = runtime.getModule(0).getModuleContext();
        BundleContext bundleContext = new BundleContextAdaptor(moduleContext);
        servletContext.setAttribute("org.osgi.framework.BundleContext", bundleContext);

        // Register the {@link Repository}, {@link Resolver}, {@link Provisioner} services
        Repository repository = registerRepositoryService(runtime);
        Resolver resolver = registerResolverService(runtime);
        registerProvisionerService(runtime, repository, resolver);
    }

    private Provisioner registerProvisionerService(Runtime runtime, Repository repository, Resolver resolver) {
        Provisioner provisioner = new DefaultProvisioner(resolver, repository) {

        };
        ModuleContext syscontext = runtime.getModule(0).getModuleContext();
        provisionerRegistration = syscontext.registerService(Provisioner.class, provisioner, null);
        return provisioner;
    }

    private Resolver registerResolverService(Runtime runtime) {
        Resolver resolver = new DefaultResolver();
        ModuleContext syscontext = runtime.getModule(0).getModuleContext();
        resolverRegistration = syscontext.registerService(Resolver.class, resolver, null);
        return resolver;
    }

    private Repository registerRepositoryService(final Runtime runtime) {
        PropertiesProvider propertyProvider = new DefaultPropertiesProvider() {
            @Override
            public Object getProperty(String key, Object defaultValue) {
                Object value = runtime.getProperty(key);
                if (value == null && Repository.PROPERTY_REPOSITORY_STORAGE_DIR.equals(key)) {
                    value = new File(catalinaWork.getPath() + File.separator + "repository").getAbsolutePath();
                }
                return value != null ? value : defaultValue;
            }
        };
        DefaultMavenIdentityRepository mavenRepo = new DefaultMavenIdentityRepository(propertyProvider);
        Repository repository = new DefaultPersistentRepository(propertyProvider, new RepositoryAggregator(mavenRepo));

        for (URL path : runtime.getModule(0).findEntries("META-INF/repository-content", "*.xml", false)) {
            try {
                RepositoryReader reader = new DefaultRepositoryXMLReader(path.openStream());
                org.jboss.gravia.resource.Resource auxres = reader.nextResource();
                while (auxres != null) {
                    RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
                    if (storage.getResource(auxres.getIdentity()) == null) {
                        storage.addResource(auxres);
                    }
                    auxres = reader.nextResource();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot install feature to repository: " + path);
            }
        }

        // Register the repository as a service
        ModuleContext syscontext = runtime.getModule(0).getModuleContext();
        repositoryRegistration = syscontext.registerService(Repository.class, repository, null);

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

    private Properties getRuntimeProperties(ServletContext servletContext) {

        Properties properties = new Properties();
        properties.setProperty(Constants.RUNTIME_TYPE, "tomcat");

        String storageClean = servletContext.getInitParameter(Constants.RUNTIME_STORAGE_CLEAN);
        if (storageClean == null) {
            storageClean = Constants.RUNTIME_STORAGE_CLEAN_ONFIRSTINIT;
        }
        properties.setProperty(Constants.RUNTIME_STORAGE_CLEAN, storageClean);

        String storageDir = servletContext.getInitParameter(Constants.RUNTIME_STORAGE);
        if (storageDir == null) {
            storageDir = new File(catalinaWork.getPath() + File.separator + Constants.RUNTIME_STORAGE_DEFAULT).getAbsolutePath();
        }
        properties.setProperty(Constants.RUNTIME_STORAGE, storageDir);

        return properties;
    }
}
