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
package org.jboss.test.gravia.runtime;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.gravia.runtime.sub.SimpleServlet;
import org.jboss.test.support.HttpRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleReference;

/**
 * Test webapp deployemnts as OSGi bundles
 *
 * @author thomas.diesler@jboss.com
 *
 * @since 07-Jun-2011
 */
@RunWith(Arquillian.class)
public class BasicPortableTestCase {

    @Before
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader instanceof BundleReference) {

        }
    }

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "simple.war");
        archive.addClasses(HttpRequest.class, SimpleServlet.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addExportPackages(BasicPortableTestCase.class);
                builder.addImportPackages(BundleActivator.class, ModuleActivator.class, Resource.class);
                builder.addImportPackages(HttpServlet.class, WebServlet.class, PostConstruct.class);
                builder.addManifestHeader("Web-ContextPath", "/simple");
                builder.addBundleClasspath("WEB-INF/classes");
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testWarDeployment() throws Exception {
        String result = performCall("/simple/servlet?input=Hello");
        Assert.assertEquals("Hello", result);
    }

    private String performCall(String path) throws Exception {
        String urlspec = "http://localhost:8080" + path;
        return HttpRequest.get(urlspec, 10, TimeUnit.SECONDS);
    }
}
