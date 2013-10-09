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
package org.jboss.gravia.runtime.tomcat;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;

/**
 * Activates the {@link Runtime} as part of the web app lifecycle.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
@WebListener
public class ApplicationActivator implements ServletContextListener {

    boolean runtimeCreated;

    /**
     * Creates the runtime and installs/starts the webapp as a module.
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        Runtime runtime = RuntimeLocator.getRuntime();
        if (runtime == null) {
            DefaultPropertiesProvider propsProvider = new DefaultPropertiesProvider();
            runtime = RuntimeLocator.createRuntime(propsProvider);
            runtimeCreated = true;
            runtime.init();
        }
        Module module = installWebappModule(runtime, servletContext);
        try {
            module.start();
        } catch (ModuleException ex) {
            throw new IllegalStateException(ex);
        }
        servletContext.setAttribute(Module.class.getName(), module);
    }

    /**
     * Uninstalls the webapp's  module.
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        Module module = (Module) servletContext.getAttribute(Module.class.getName());
        if (module != null && module.getState() != Module.State.UNINSTALLED) {
            module.uninstall();
        }
        if (runtimeCreated) {
            RuntimeLocator.releaseRuntime();
            runtimeCreated = false;
        }
    }

    private Module installWebappModule(Runtime runtime, ServletContext servletContext) {
        Module module;
        try {
            ClassLoader classLoader = ApplicationActivator.class.getClassLoader();
            Manifest manifest = getWebappManifest(servletContext);
            ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(manifest);
            module = runtime.installModule(classLoader, headersProvider.getHeaders());
            servletContext.setAttribute(Module.class.getName(), module);
        } catch (RuntimeException rte) {
            throw rte;
        } catch (ModuleException ex) {
            throw new IllegalStateException(ex);
        }
        return module;
    }

    private Manifest getWebappManifest(ServletContext servletContext) {
        Manifest manifest;
        try {
            URL entry = servletContext.getResource("/" + JarFile.MANIFEST_NAME);
            manifest = new Manifest(entry.openStream());
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read manifest", ex);
        }
        return manifest;
    }
}
