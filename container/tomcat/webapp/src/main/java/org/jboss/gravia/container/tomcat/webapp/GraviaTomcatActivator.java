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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.gravia.container.tomcat.support.TomcatPropertiesProvider;
import org.jboss.gravia.container.tomcat.support.TomcatResourceInstaller;
import org.jboss.gravia.container.tomcat.support.TomcatRuntimeFactory;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.WebAppContextListener;

/**
 * Activates the {@link Runtime} as part of the web app lifecycle.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Nov-2013
 */
@WebListener
public class GraviaTomcatActivator implements ServletContextListener {

    private Set<ServiceRegistration<?>> registrations = new HashSet<ServiceRegistration<?>>();

    @Override
    public void contextInitialized(ServletContextEvent event) {

        // Create the runtime
        ServletContext servletContext = event.getServletContext();
        TomcatPropertiesProvider propsProvider = new TomcatPropertiesProvider(servletContext);
        Runtime runtime = RuntimeLocator.createRuntime(new TomcatRuntimeFactory(servletContext), propsProvider);
        runtime.init();

        // Register the {@link RuntimeEnvironment}, {@link ResourceInstaller} services
        registerServices(servletContext, runtime);

        // Install and start this webapp as a module
        WebAppContextListener webappInstaller = new WebAppContextListener();
        Module module = webappInstaller.installWebappModule(servletContext);
        servletContext.setAttribute(Module.class.getName(), module);
        try {
            module.start();
        } catch (ModuleException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void registerServices(ServletContext servletContext, Runtime runtime) {
        RuntimeEnvironment environment = new RuntimeEnvironment(runtime).initDefaultContent();
        TomcatResourceInstaller installer = new TomcatResourceInstaller(environment);
        ModuleContext syscontext = runtime.getModuleContext();
        registrations.add(syscontext.registerService(RuntimeEnvironment.class, environment, null));
        registrations.add(syscontext.registerService(ResourceInstaller.class, installer, null));
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        for (ServiceRegistration<?> sreg : registrations) {
            sreg.unregister();
        }
    }
}
