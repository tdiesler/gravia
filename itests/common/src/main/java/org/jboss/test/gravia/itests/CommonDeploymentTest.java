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
package org.jboss.test.gravia.itests;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.gravia.itests.sub.ApplicationActivator;
import org.jboss.test.gravia.itests.sub.SimpleServlet;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleActivator;

/**
 * Test webapp deployemnts
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Oct-2013
 */
public abstract class CommonDeploymentTest {

    public static WebArchive deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "common.war");
        archive.addClasses(HttpRequest.class, ApplicationActivator.class, SimpleServlet.class);
        archive.addClasses(CommonDeploymentTest.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(BundleActivator.class, ModuleActivator.class, Resource.class);
                builder.addImportPackages(Servlet.class, HttpServlet.class, WebServlet.class);
                builder.addManifestHeader(ManifestBuilder.GRAVIA_IDENTITY_CAPABILITY, archive.getName() + ";version=1.0.0");
                builder.addManifestHeader("Web-ContextPath", "/common");
                builder.addBundleClasspath("WEB-INF/classes");
                builder.addManifestHeader("Dependencies", "org.jboss.gravia"); // WildFly
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testServletAccess() throws Exception {
        String result = performCall("/common/servlet?input=Hello");
        Assert.assertEquals("Hello from Module[common.war:1.0.0]", result);
    }

    @Test
    public void testModuleLifecycle() throws Exception {

        // With forkMode=always make sure we have an initialized runtime
        String result = performCall("/common/servlet?input=Hello");
        Assert.assertEquals("Hello from Module[common.war:1.0.0]", result);

        Module modA = RuntimeLocator.getRuntime().getModule(getClass().getClassLoader());
        Assert.assertEquals(Module.State.ACTIVE, modA.getState());

        ModuleContext context = modA.getModuleContext();
        ServiceRegistration<String> sreg = context.registerService(String.class, new String("Hello"), null);
        Assert.assertNotNull("Null sreg", sreg);

        String service = context.getService(sreg.getReference());
        Assert.assertEquals("Hello", service);

        modA.stop();
        Assert.assertEquals(Module.State.RESOLVED, modA.getState());

        modA.uninstall();
        Assert.assertEquals(Module.State.UNINSTALLED, modA.getState());

        try {
            modA.start();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    protected String performCall(String path) throws Exception {
        String urlspec = "http://localhost:8080" + path;
        return HttpRequest.get(urlspec, 10, TimeUnit.SECONDS);
    }
}
