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
package org.jboss.test.gravia.itests.karaf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.container.tomcat.extension.ModuleLifecycleListener;
import org.jboss.gravia.resource.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.embedded.spi.HttpServiceProxyListener;
import org.jboss.gravia.runtime.embedded.spi.HttpServiceProxyServlet;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.http.HttpService;

/**
 * Test the {@link HttpService}
 *
 * @author thomas.diesler@jboss.com
 * @since 28-Nov-2013
 */
@RunWith(Arquillian.class)
public class HttpServiceTestCase {

    static StringAsset STRING_ASSET = new StringAsset("Hello from Resource");

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "http-service.war");
        archive.addClasses(ModuleLifecycleListener.class, HttpServiceProxyServlet.class, HttpServiceProxyListener.class);
        archive.addClasses(HttpRequest.class);
        archive.addAsResource(STRING_ASSET, "res/message.txt");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName("http-service");
                builder.addBundleVersion("1.0.0");
                builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                builder.addImportPackages(RuntimeLocator.class, Servlet.class, HttpServlet.class, HttpService.class);
                builder.addBundleClasspath("WEB-INF/classes");
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testServletAccess() throws Exception {
        Runtime runtime = RuntimeLocator.getRuntime();
        Module module = runtime.getModule(getClass().getClassLoader());
        ModuleContext context = module.getModuleContext();
        ServiceReference<HttpService> sref = context.getServiceReference(HttpService.class);
        HttpService httpService = context.getService(sref);
        String reqspec = "/service?test=param&param=Kermit";
        try {
            // Verify that the alias is not yet available
            assertNotAvailable(reqspec);

            // Register the test servlet and make a call
            httpService.registerServlet("/service", new HttpServiceServlet(module), null, null);
            Assert.assertEquals("Hello: Kermit", performCall(reqspec));

            // Unregister the servlet alias
            httpService.unregister("/service");
            assertNotAvailable(reqspec);

            // Verify that the alias is not available any more
            assertNotAvailable(reqspec);
        } finally {
            context.ungetService(sref);
        }
    }

    @Test
    public void testResourceAccess() throws Exception {
        Runtime runtime = RuntimeLocator.getRuntime();
        Module module = runtime.getModule(getClass().getClassLoader());
        ModuleContext context = module.getModuleContext();
        ServiceReference<HttpService> sref = context.getServiceReference(HttpService.class);
        HttpService httpService = context.getService(sref);
        String reqspec = "/resource/message.txt";
        try {
            // Verify that the alias is not yet available
            assertNotAvailable(reqspec);

            // Register the test resource and make a call
            httpService.registerResources("/resource", "/res", null);
            Assert.assertEquals("Hello from Resource", performCall(reqspec));

            // Unregister the servlet alias
            httpService.unregister("/resource");

            // Verify that the alias is not available any more
            assertNotAvailable(reqspec);
        } finally {
            context.ungetService(sref);
        }
    }

    private void assertNotAvailable(String reqspec) throws Exception {
        try {
            performCall(reqspec);
            Assert.fail("IOException expected");
        } catch (IOException ex) {
            // expected
        }
    }

    private String performCall(String path) throws Exception {
        return HttpRequest.get("http://localhost:8080" + path, 2, TimeUnit.SECONDS);
    }

    @SuppressWarnings("serial")
    static final class HttpServiceServlet extends HttpServlet {

        private final Module module;

        // This hides the default ctor and verifies that this instance is used
        HttpServiceServlet(Module module) {
            this.module = module;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
            PrintWriter out = res.getWriter();
            String type = req.getParameter("test");
            if ("param".equals(type)) {
                String value = req.getParameter("param");
                out.print("Hello: " + value);
            } else if ("init".equals(type)) {
                String key = req.getParameter("init");
                String value = getInitParameter(key);
                out.print(key + "=" + value);
            } else if ("instance".equals(type)) {
                out.print(module.getIdentity());
            } else {
                throw new IllegalArgumentException("Invalid 'test' parameter: " + type);
            }
            out.close();
        }
    }
}