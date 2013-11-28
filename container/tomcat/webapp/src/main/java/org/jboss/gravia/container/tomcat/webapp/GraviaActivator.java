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
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.gravia.container.tomcat.extension.TomcatRuntimeFactory;
import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.embedded.spi.BundleContextAdaptor;
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

    @Override
    public void contextInitialized(ServletContextEvent event) {

        // Create the runtime
        Properties sysprops = getRuntimeProperties();
        DefaultPropertiesProvider propsProvider = new DefaultPropertiesProvider(sysprops, true);
        Runtime runtime = RuntimeLocator.createRuntime(new TomcatRuntimeFactory(), propsProvider);
        runtime.init();

        // HttpService integration
        ServletContext servletContext = event.getServletContext();
        ModuleContext moduleContext = runtime.getModule(0).getModuleContext();
        BundleContext bundleContext = new BundleContextAdaptor(moduleContext);
        servletContext.setAttribute("org.osgi.framework.BundleContext", bundleContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    private Properties getRuntimeProperties() {

        // Gravia integration properties
        File catalinaWork = new File(catalinaHome.getPath() + File.separator + "work");
        File storageDir = new File(catalinaWork.getPath() + File.separator + Constants.RUNTIME_STORAGE_DEFAULT);

        Properties properties = new Properties();
        properties.setProperty(Constants.RUNTIME_STORAGE_CLEAN, Constants.RUNTIME_STORAGE_CLEAN_ONFIRSTINIT);
        properties.setProperty(Constants.RUNTIME_STORAGE, storageDir.getAbsolutePath());
        return properties;
    }
}
