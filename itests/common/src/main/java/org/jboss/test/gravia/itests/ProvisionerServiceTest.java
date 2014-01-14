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
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.repository.Repository;
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

    static final String DEPLOYMENT_A = "deploymentA";
    static final String RESOURCE_A = "resourceA";

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
                    builder.addImportPackages(Runtime.class, Provisioner.class, Resource.class, Repository.class);
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

        List<ResourceHandle> handles = new ArrayList<ResourceHandle>();
        for (ResourceHandle handle : result) {
            handles.add(handle);
        }

        try {
            // Deploy a resource through the {@link ResourceInstaller}
            DefaultResourceBuilder builder = new DefaultResourceBuilder();
            Capability icap = builder.addIdentityCapability(DEPLOYMENT_A, Version.emptyVersion);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE, DEPLOYMENT_A + ".war");
            builder.addContentCapability(deployer.getDeployment(DEPLOYMENT_A));
            Resource resource = builder.getResource();

            ResourceInstaller installer = provisioner.getResourceInstaller();
            handles.add(installer.installResource(resource, null));

            String reqspec = "/service?test=Kermit";
            String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/" + DEPLOYMENT_A;
            Assert.assertEquals("Hello Kermit", performCall(context, reqspec));
        } finally {
            for (ResourceHandle handle : handles) {
                handle.uninstall();
            }
        }
    }

    @Test
    public void testProvisionResources() throws Exception {

        // Add a resource to the repository that has a dependency on camel.core
        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        Capability icap = builder.addIdentityCapability(RESOURCE_A, Version.emptyVersion);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE, RESOURCE_A + ".war");
        builder.addContentCapability(deployer.getDeployment(RESOURCE_A));
        builder.addIdentityRequirement("org.apache.camel.core", new VersionRange("[2.11,3.0)"));
        Resource res = builder.getResource();

        Repository repository = provisioner.getRepository();
        Resource resA = repository.addResource(res);
        Assert.assertEquals(RESOURCE_A + ":0.0.0", resA.getIdentity().toString());

        try {
            // Provision that resource, which should first install camel.core and then deploy deploymentA
            // The {@link ModuleActivator} of deploymentA should register an http service that accesses camel
            Requirement req = new IdentityRequirementBuilder(resA.getIdentity()).getRequirement();
            Set<ResourceHandle> result = provisioner.provisionResources(Collections.singleton(req));
            Assert.assertFalse(result.isEmpty());

            List<ResourceHandle> handles = new ArrayList<ResourceHandle>();
            for (ResourceHandle handle : result) {
                handles.add(handle);
            }

            try {
                String reqspec = "/service?test=Kermit";
                String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/" + RESOURCE_A;
                Assert.assertEquals("Hello Kermit", performCall(context, reqspec));
            } finally {
                for (ResourceHandle handle : handles) {
                    handle.uninstall();
                }
            }
        } finally {
            repository.removeResource(resA.getIdentity());
        }
    }

    private String performCall(String context, String path) throws Exception {
        return performCall(context, path, null, 2, TimeUnit.SECONDS);
    }

    private String performCall(String context, String path, Map<String, String> headers, long timeout, TimeUnit unit) throws Exception {
        return HttpRequest.get("http://localhost:8080" + context + path, headers, timeout, unit);
    }

    @Deployment(name = DEPLOYMENT_A, managed = false, testable = false)
    public static Archive<?> getDeployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_A + ".war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(CamelTransformActivator.class, ModuleActivatorBridge.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(DEPLOYMENT_A);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformActivator.class.getName());
                    builder.addImportPackages(ModuleActivatorBridge.class, Runtime.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(CamelContext.class, DefaultCamelContext.class, RouteBuilder.class, RouteDefinition.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(DEPLOYMENT_A, Version.emptyVersion);
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

    @Deployment(name = RESOURCE_A, managed = false, testable = false)
    public static Archive<?> getRepositoryResource() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, RESOURCE_A + ".war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(CamelTransformActivator.class, ModuleActivatorBridge.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(RESOURCE_A);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformActivator.class.getName());
                    builder.addImportPackages(ModuleActivatorBridge.class, Runtime.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(CamelContext.class, DefaultCamelContext.class, RouteBuilder.class, RouteDefinition.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(RESOURCE_A, Version.emptyVersion);
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
