/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Dictionary;
import java.util.Hashtable;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.Constants;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.utils.IOUtils;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.test.gravia.itests.support.AnnotatedContextListener;
import org.jboss.test.gravia.itests.support.ArchiveBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test URLStreamHandler integration
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2014
 */
@RunWith(Arquillian.class)
public class URLStreamHandlerTestCase {

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("url-handler-test");
        archive.addClasses(RuntimeType.TOMCAT, AnnotatedContextListener.class);
        archive.addClasses(URLStreamHandlerTestCase.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(RuntimeLocator.class, IOUtils.class);
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(archive.getName(), "1.0.0");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Test
    public void testURLHandlerService() throws Exception {

        try {
            new URL("foo://hi");
            Assert.fail("No handler registered. Should throw a MalformedURLException.");
        } catch (MalformedURLException mue) {
            // expected
        }

        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        ModuleContext syscontext = runtime.getModuleContext();

        Dictionary<String, Object> props = new Hashtable<>();
        props.put(Constants.URL_HANDLER_PROTOCOL, "foo");
        ServiceRegistration<URLStreamHandler> sreg = syscontext.registerService(URLStreamHandler.class, new MyHandler(), props);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copyStream(new URL("foo://somehost").openStream(), baos);
            Assert.assertEquals("somehost", new String(baos.toByteArray()));
        } finally {
            sreg.unregister();
        }

        try {
            new URL("foo://hi").openConnection();
            Assert.fail("No handler registered any more.");
        } catch (IOException ex) {
            // expected
        }
    }

    private static class MyHandler extends URLStreamHandler {

        @Override
        public URLConnection openConnection(final URL url) throws IOException {
            return new URLConnection(url) {

                @Override
                public void connect() throws IOException {
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(url.getHost().getBytes());
                }
            };
        }
    }
}
