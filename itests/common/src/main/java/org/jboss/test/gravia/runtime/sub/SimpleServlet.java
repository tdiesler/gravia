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
package org.jboss.test.gravia.runtime.sub;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;

@SuppressWarnings("serial")
@WebServlet(name = "SimpleServlet", urlPatterns = { "/servlet" })
public class SimpleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String msg = req.getParameter("input");
        Writer writer = resp.getWriter();
        Module module = getWebappModule(getServletContext());
        writer.write((msg != null ? msg : "No input") + " from " + module);
    }

    private Module getWebappModule() {
        // Initialized in {@link ApplicationActivator}
        return (Module) getServletContext().getAttribute(Module.class.getName());
    }

    private Module getWebappModule(ServletContext servletContext) {
        Module module = (Module) getServletContext().getAttribute(Module.class.getName());
        if (module == null) {
            Runtime runtime = RuntimeLocator.locateRuntime(null);
            Manifest manifest = getWebappManifest(servletContext);
            module = runtime.installModule(getClass().getClassLoader(), manifest);
            servletContext.setAttribute(Module.class.getName(), module);
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
