/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
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
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.gravia.runtime.spi.NamedResourceAssociation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Register the Webapp as a {@link Module}.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class WebAppContextListener implements ServletContextListener {

    /** The attachment key for the ServletContext */
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
            module = installWebappModule(servletContext);
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
            servletContext.setAttribute(BundleContext.class.getName(), bundleContext);
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

    /**
     * Install a webapp module from the given servlet context.
     */
    public Module installWebappModule(ServletContext servletContext) {

        Manifest manifest = getWebappManifest(servletContext);
        if (manifest == null)
            return null;

        ResourceBuilder resbuilder = new ManifestResourceBuilder().load(manifest);
        if (!resbuilder.isValid())
            return null;

        Resource resource = resbuilder.getResource();

        Dictionary<String, String> headers = new ManifestHeadersProvider(manifest).getHeaders();
        return installWebappModule(servletContext, resource, headers);
    }

    private Module installWebappModule(ServletContext servletContext, Resource resource, Dictionary<String, String> headers) {

        String contextPath = servletContext.getContextPath();
        Resource association = NamedResourceAssociation.getResource(contextPath);
        resource = association != null ? association : resource;

        AttachableSupport context = new AttachableSupport();
        context.putAttachment(SERVLET_CONTEXT_KEY, servletContext);

        Module module;
        try {
            Runtime runtime = RuntimeLocator.getRequiredRuntime();
            ClassLoader classLoader = servletContext.getClassLoader();
            module = runtime.installModule(classLoader, resource, headers, context);
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
