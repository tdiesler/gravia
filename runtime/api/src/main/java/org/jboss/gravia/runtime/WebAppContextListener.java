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
package org.jboss.gravia.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.resource.ManifestResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.spi.AttachableSupport;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Register the Webapp as a {@link Module}.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class WebAppContextListener implements ServletContextListener {

    public static final AttachmentKey<ServletContext> SERVLET_CONTEXT_KEY = AttachmentKey.create(ServletContext.class);

    /**
     * Installs/starts the webapp as a module.
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {

        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        ServletContext servletContext = event.getServletContext();
        Module module = runtime.getModule(servletContext.getClassLoader());

        // Install the module
        if (module == null) {
            module = installWebappModule(runtime, servletContext);
        }

        // Start the module
        if (module != null) {
            servletContext.setAttribute(Module.class.getName(), module);

            try {
                module.start();
            } catch (ModuleException ex) {
                throw new IllegalStateException(ex);
            }

            // HttpService integration
            BundleContext bundleContext = module.adapt(Bundle.class).getBundleContext();
            servletContext.setAttribute("org.osgi.framework.BundleContext", bundleContext);
        }
    }

    /**
     * Uninstalls the webapp's module.
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        Module module = (Module) servletContext.getAttribute(Module.class.getName());
        if (module != null && module.getState() != Module.State.UNINSTALLED) {
            module.uninstall();
        }
    }

    public Module installWebappModule(Runtime runtime, ServletContext servletContext) {
        ClassLoader classLoader = servletContext.getClassLoader();
        Manifest manifest = getWebappManifest(servletContext);
        if (manifest == null)
            return null;

        ResourceBuilder resbuilder = new ManifestResourceBuilder().load(manifest);
        if (resbuilder.isValid() == false)
            return null;

        AttachableSupport context = new AttachableSupport();
        context.putAttachment(SERVLET_CONTEXT_KEY, servletContext);

        Module module;
        try {
            Resource resource = resbuilder.getResource();
            ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(manifest);
            module = runtime.installModule(classLoader, resource, headersProvider.getHeaders(), context);
        } catch (RuntimeException rte) {
            throw rte;
        } catch (ModuleException ex) {
            throw new IllegalStateException(ex);
        }
        return module;
    }

    private Manifest getWebappManifest(ServletContext servletContext) {
        Manifest manifest = null;
        try {
            URL entry = servletContext.getResource("/" + JarFile.MANIFEST_NAME);
            if (entry != null) {
                manifest = new Manifest(entry.openStream());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read manifest", ex);
        }
        return manifest;
    }
}
