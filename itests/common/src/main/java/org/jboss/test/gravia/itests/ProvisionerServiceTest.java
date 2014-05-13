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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.Constants;
import org.jboss.gravia.arquillian.container.ContainerSetup;
import org.jboss.gravia.arquillian.container.ContainerSetupTask;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.runtime.ModuleActivatorBridge;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.WebAppContextListener;
import org.jboss.gravia.runtime.Wiring;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.test.gravia.itests.sub.b.CamelTransformActivator;
import org.jboss.test.gravia.itests.support.AnnotatedContextListener;
import org.jboss.test.gravia.itests.support.AnnotatedProxyListener;
import org.jboss.test.gravia.itests.support.AnnotatedProxyServlet;
import org.jboss.test.gravia.itests.support.ArchiveBuilder;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.http.HttpService;

/**
 * Test {@link Provisioner} service.
 *
 * @author thomas.diesler@jboss.com
 * @since 19-Dec-2013
 */
@RunWith(Arquillian.class)
@ContainerSetup(ProvisionerServiceTest.Setup.class)
public class ProvisionerServiceTest {

    static final String DEPLOYMENT_B = "deploymentB";
    static final String RESOURCE_B = "resourceB";

    public static class Setup extends ContainerSetupTask {
        protected String[] getInitialFeatureNames() {
            return new String[] { "camel.core" };
        }
    }

    @ArquillianResource
    Deployer deployer;

    private Provisioner provisioner;

    @Deployment
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("provisioner-service");
        archive.addClasses(HttpRequest.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addImportPackages(Runtime.class, Resource.class, Provisioner.class, Resolver.class, Repository.class);
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

    @Before
    public void setUp() throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        ModuleContext syscontext = runtime.getModuleContext();
        ServiceReference<Provisioner> sref = syscontext.getServiceReference(Provisioner.class);
        Assert.assertNotNull("Provisioner reference not null", sref);
        provisioner = syscontext.getService(sref);
    }

    @Test
    public void testDeploymentWithDependency() throws Exception {

        // Provision the camel.core feature
        ResourceIdentity identity = ResourceIdentity.fromString("camel.core.feature:0.0.0");
        Requirement req = new IdentityRequirementBuilder(identity).getRequirement();
        Set<ResourceHandle> result = provisioner.provisionResources(Collections.singleton(req));

        List<ResourceHandle> handles = new ArrayList<ResourceHandle>(result);
        try {
            // Verify the wiring
            Environment environment = provisioner.getEnvironment();
            Map<Resource, Wiring> wirings = environment.getWirings();
            ResourceIdentity residA = ResourceIdentity.create("org.apache.camel.core", "2.11.0");
            Resource resA = environment.getResource(residA);
            Assert.assertNotNull("Resource in environment", resA);
            Wiring wiringA = wirings.get(resA);
            Assert.assertNotNull("Wiring in environment", wiringA);
            Assert.assertEquals("Two required wires", 2, wiringA.getRequiredResourceWires(null).size());

            // Build a resource that has a class loading dependency
            DefaultResourceBuilder builder = new DefaultResourceBuilder();
            ResourceIdentity residB = ResourceIdentity.create(DEPLOYMENT_B, Version.emptyVersion);
            Capability icap = builder.addIdentityCapability(residB);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE, DEPLOYMENT_B + ".war");
            builder.addContentCapability(deployer.getDeployment(DEPLOYMENT_B));
            Resource resB = builder.getResource();

            // Deploy a resource through the {@link ResourceInstaller}
            handles.add(provisioner.installResource(resB, null));
            Assert.assertTrue("At least one resource", handles.size() > 0);

            // Make a call to the HttpService endpoint that goes through a Camel route
            String reqspec = "/service?test=Kermit";
            String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/" + DEPLOYMENT_B;
            Assert.assertEquals("Hello Kermit", performCall(context, reqspec));

            // Verify module available
            Runtime runtime = RuntimeLocator.getRequiredRuntime();
            Assert.assertNotNull("Module available", runtime.getModule(residA));
            Assert.assertNotNull("Module available", runtime.getModule(residB));

            // Verify the wiring
            wirings = environment.getWirings();
            resA = environment.getResource(residA);
            Assert.assertNotNull("Resource in environment", resA);
            wiringA = wirings.get(resA);
            Assert.assertEquals("Two required wires", 2, wiringA.getRequiredResourceWires(null).size());

            // Deployment did not go through the {@link Provisioner} service
            // There is no wiring
            resB = environment.getResource(residB);
            Assert.assertNotNull("Resource in environment", resB);
            Wiring wiringB = wirings.get(resB);
            Assert.assertNull("Wiring not in environment", wiringB);
        } finally {
            for (ResourceHandle handle : handles) {
                handle.uninstall();
            }
        }
    }

    @Test
    public void testProvisionResources() throws Exception {

        // Build a resource to the repository that has a dependency on camel.core
        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        Capability icap = builder.addIdentityCapability(RESOURCE_B, Version.emptyVersion);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE, RESOURCE_B + ".war");
        builder.addContentCapability(deployer.getDeployment(RESOURCE_B));
        builder.addIdentityRequirement("org.apache.camel.core", new VersionRange("[2.11,3.0)"));
        Resource res = builder.getResource();

        // Add that resource to the repository
        Repository repository = provisioner.getRepository();
        Resource resB = repository.addResource(res);
        Assert.assertEquals(RESOURCE_B + ":0.0.0", resB.getIdentity().toString());

        try {
            // Provision that resource
            Requirement req = new IdentityRequirementBuilder(resB.getIdentity()).getRequirement();
            Set<ResourceHandle> result = provisioner.provisionResources(Collections.singleton(req));

            List<ResourceHandle> handles = new ArrayList<ResourceHandle>(result);
            try {
                // Make a call to the HttpService endpoint that goes through a Camel route
                String reqspec = "/service?test=Kermit";
                String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/" + RESOURCE_B;
                Assert.assertEquals("Hello Kermit", performCall(context, reqspec));

                // Verify module available
                Runtime runtime = RuntimeLocator.getRequiredRuntime();
                ResourceIdentity residA = ResourceIdentity.create("org.apache.camel.core", "2.11.0");
                Assert.assertNotNull("Module available", runtime.getModule(residA));
                ResourceIdentity residB = ResourceIdentity.create(RESOURCE_B, Version.emptyVersion);
                Assert.assertNotNull("Module available", runtime.getModule(residB));

                // Verify the wiring
                Environment environment = provisioner.getEnvironment();
                Map<Resource, Wiring> wirings = environment.getWirings();
                Resource resA = environment.getResource(residA);
                Assert.assertNotNull("Resource in environment", resA);
                Wiring wiringA = wirings.get(resA);
                Assert.assertNotNull("Wiring in environment", wiringA);
                Assert.assertEquals("Two required wires", 2, wiringA.getRequiredResourceWires(null).size());
                Assert.assertEquals("One provided wires", 1, wiringA.getProvidedResourceWires(null).size());
                resB = environment.getResource(residB);
                Assert.assertNotNull("Resource in environment", resB);
                Wiring wiringB = wirings.get(resB);
                Assert.assertNotNull("Wiring in environment", wiringB);
                Assert.assertEquals("One required wires", 1, wiringB.getRequiredResourceWires(null).size());
                Assert.assertEquals("Zero provided wires", 0, wiringB.getProvidedResourceWires(null).size());
            } finally {
                for (ResourceHandle handle : handles) {
                    handle.uninstall();
                }
            }
        } finally {
            repository.removeResource(resB.getIdentity());
        }
    }

    private String performCall(String context, String path) throws Exception {
        return performCall(context, path, null, 2, TimeUnit.SECONDS);
    }

    private String performCall(String context, String path, Map<String, String> headers, long timeout, TimeUnit unit) throws Exception {
        return HttpRequest.get("http://localhost:8080" + context + path, headers, timeout, unit);
    }

    @Deployment(name = DEPLOYMENT_B, managed = false, testable = false)
    public static Archive<?> getDeployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_B + ".war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(CamelTransformActivator.class, ModuleActivatorBridge.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(DEPLOYMENT_B);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformActivator.class.getName());
                    builder.addImportPackages(ModuleActivatorBridge.class, Runtime.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(CamelContext.class, DefaultCamelContext.class, RouteBuilder.class, RouteDefinition.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(DEPLOYMENT_B, Version.emptyVersion);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformActivator.class.getName());
                    builder.addManifestHeader("Dependencies", "org.apache.camel.core");
                    return builder.openStream();
                }
            }
        });
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.apache.felix:org.apache.felix.http.proxy").withoutTransitivity().asFile();
        archive.addAsLibraries(libs);
        return archive;
    }

    @Deployment(name = RESOURCE_B, managed = false, testable = false)
    public static Archive<?> getRepositoryResource() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, RESOURCE_B + ".war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(CamelTransformActivator.class, ModuleActivatorBridge.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(RESOURCE_B);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformActivator.class.getName());
                    builder.addImportPackages(ModuleActivatorBridge.class, Runtime.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(CamelContext.class, DefaultCamelContext.class, RouteBuilder.class, RouteDefinition.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(RESOURCE_B, Version.emptyVersion);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformActivator.class.getName());
                    return builder.openStream();
                }
            }
        });
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.apache.felix:org.apache.felix.http.proxy").withoutTransitivity().asFile();
        archive.addAsLibraries(libs);
        return archive;
    }
}
