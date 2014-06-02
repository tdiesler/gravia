/*
 * #%L
 * Gravia :: Integration Tests :: Common
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.test.gravia.itests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginContext;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.container.tomcat.extension.UserDatabaseLoginModule;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.WebAppContextListener;
import org.jboss.gravia.utils.Base64Encoder;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.test.gravia.itests.support.AnnotatedContextListener;
import org.jboss.test.gravia.itests.support.AnnotatedProxyListener;
import org.jboss.test.gravia.itests.support.AnnotatedProxyServlet;
import org.jboss.test.gravia.itests.support.ArchiveBuilder;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.jboss.test.gravia.itests.support.SecureHttpContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;

/**
 * Test a JAAS secured {@link HttpService}
 *
 * @author thomas.diesler@jboss.com
 * @since 03-Dec-2013
 */
@RunWith(Arquillian.class)
public class HttpServiceSecureTestCase {

    static StringAsset STRING_ASSET = new StringAsset("Hello from Resource");

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "http-service-secure.war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(HttpRequest.class, SecureHttpContext.class, Base64Encoder.class);
        archive.addClasses(UserDatabaseLoginModule.class);
        archive.addAsResource(STRING_ASSET, "res/message.txt");
        archive.addAsWebResource("OSGI-INF/blueprint/gravia-jaas-realm.xml", "OSGI-INF/blueprint/gravia-jaas-realm.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(RuntimeLocator.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(Subject.class, Callback.class, LoginContext.class);
                    builder.addImportPackage("org.apache.karaf.jaas.config");
                    builder.addImportPackage("org.apache.karaf.jaas.modules");
                    builder.addImportPackage("org.apache.karaf.jaas.modules.encryption");
                    builder.addImportPackage("org.apache.karaf.jaas.modules.properties");
                    builder.addImportPackages(Logger.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability("http-service-secure", "1.0.0");
                    return builder.openStream();
                }
            }
        });
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.apache.felix:org.apache.felix.http.proxy").withoutTransitivity().asFile();
        archive.addAsLibraries(libs);
        return archive;
    }

    @Test
    public void testServletAccess() throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(getClass().getClassLoader());
        ModuleContext context = module.getModuleContext();
        ServiceReference<HttpService> sref = context.getServiceReference(HttpService.class);
        HttpService httpService = context.getService(sref);
        String reqspec = "/service?test=param&param=Kermit";
        try {
            Map<String, String> headers = Collections.singletonMap("Authorization", "Basic " + Base64Encoder.encode("graviaUser:graviaPass"));

            // Verify that the alias is not yet available
            assertNotAvailable(reqspec, headers);

            HttpContext base = httpService.createDefaultHttpContext();
            String realm = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "gravia" : "ApplicationRealm";
            HttpContext secureContext = new SecureHttpContext(base, realm, "graviaRole");

            // Register the test servlet and make a call
            httpService.registerServlet("/service", new HttpServiceServlet(module), null, secureContext);
            Assert.assertEquals("Hello: Kermit", performCall(reqspec, headers));

            // Unregister the servlet alias
            httpService.unregister("/service");
            assertNotAvailable(reqspec, headers);

            // Verify that the alias is not available any more
            assertNotAvailable(reqspec, headers);
        } finally {
            context.ungetService(sref);
        }
    }

    @Test
    public void testResourceAccess() throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(getClass().getClassLoader());
        ModuleContext context = module.getModuleContext();
        ServiceReference<HttpService> sref = context.getServiceReference(HttpService.class);
        HttpService httpService = context.getService(sref);
        String reqspec = "/resource/message.txt";
        try {
            Map<String, String> headers = Collections.singletonMap("Authorization", "Basic " + Base64Encoder.encode("graviaUser:graviaPass"));

            // Verify that the alias is not yet available
            assertNotAvailable(reqspec, headers);

            HttpContext base = httpService.createDefaultHttpContext();
            String realm = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "gravia" : "ApplicationRealm";
            HttpContext secureContext = new SecureHttpContext(base, realm, "graviaRole");

            // Register the test resource and make a call
            httpService.registerResources("/resource", "/res", secureContext);
            Assert.assertEquals("Hello from Resource", performCall(reqspec, headers));

            // Unregister the servlet alias
            httpService.unregister("/resource");

            // Verify that the alias is not available any more
            assertNotAvailable(reqspec, headers);
        } finally {
            context.ungetService(sref);
        }
    }

    private void assertNotAvailable(String reqspec, Map<String, String> headers) throws Exception {
        try {
            performCall(reqspec, headers, 500, TimeUnit.MILLISECONDS);
            Assert.fail("IOException expected");
        } catch (IOException ex) {
            // expected
        }
    }

    private String performCall(String path, Map<String, String> headers) throws Exception {
        return performCall(path, headers, 2, TimeUnit.SECONDS);
    }

    private String performCall(String path, Map<String, String> headers, long timeout, TimeUnit unit) throws Exception {
        Object port = RuntimeLocator.getRequiredRuntime().getProperty("org.osgi.service.http.port", "8080");
        String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/http-service-secure";
        return HttpRequest.get("http://localhost:" + port + context + path, headers, timeout, unit);
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
