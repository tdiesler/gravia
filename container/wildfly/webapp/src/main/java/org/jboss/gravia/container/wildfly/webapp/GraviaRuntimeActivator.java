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
package org.jboss.gravia.container.wildfly.webapp;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Activates the {@link Runtime} as part of the web app lifecycle.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Nov-2013
 */
public class GraviaRuntimeActivator implements ServletContextListener {

    private Set<ServiceRegistration<?>> registrations = new HashSet<ServiceRegistration<?>>();

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();

        // HttpService integration
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module sysmodule = runtime.getModuleContext().getModule();
        BundleContext bundleContext = sysmodule.adapt(Bundle.class).getBundleContext();
        servletContext.setAttribute(BundleContext.class.getName(), bundleContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        for (ServiceRegistration<?> sreg : registrations) {
            sreg.unregister();
        }
    }
}
