/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.gravia.itests.sub;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.gravia.runtime.ManifestHeadersProvider;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;

// WebAppParser cannot load @WebListener class
// https://ops4j1.jira.com/browse/PAXWEB-627

//@WebListener

public class ApplicationActivator implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        Module module = getWebappModule(servletContext);
        servletContext.setAttribute(Module.class.getName(), module);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    static Module getWebappModule(ServletContext servletContext) {
        Module module = (Module) servletContext.getAttribute(Module.class.getName());
        if (module == null) {
            Runtime runtime = RuntimeLocator.locateRuntime(null);
            ClassLoader classLoader = ApplicationActivator.class.getClassLoader();
            Manifest manifest = getWebappManifest(servletContext);
            ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(manifest);
            module = runtime.installModule(classLoader, headersProvider.getHeaders());
            servletContext.setAttribute(Module.class.getName(), module);
        }
        return module;
    }

    static Manifest getWebappManifest(ServletContext servletContext) {
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
