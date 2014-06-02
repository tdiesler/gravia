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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.Constants;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.test.gravia.itests.sub.a.ServiceA;
import org.jboss.test.gravia.itests.sub.a1.ServiceA1;
import org.jboss.test.gravia.itests.support.AnnotatedContextListener;
import org.jboss.test.gravia.itests.support.ArchiveBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.component.ComponentContext;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Oct-2013
 */
@RunWith(Arquillian.class)
public class ServiceComponentTest  {

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("scr-test");
        archive.addClasses(ServiceComponentTest.class);
        archive.addClasses(RuntimeType.TOMCAT, AnnotatedContextListener.class);
        archive.addClasses(ServiceA.class, ServiceA1.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.a.ServiceA.xml");
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.a1.ServiceA1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(RuntimeLocator.class, ComponentContext.class, Resource.class);
                    builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.itests.sub.a.ServiceA.xml,OSGI-INF/org.jboss.test.gravia.itests.sub.a1.ServiceA1.xml");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(archive.getName(), "1.0.0");
                    builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.itests.sub.a.ServiceA.xml,OSGI-INF/org.jboss.test.gravia.itests.sub.a1.ServiceA1.xml");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Test
    public void testServiceAvailability() throws Exception {

        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module modA = runtime.getModule(getClass().getClassLoader());

        ModuleContext ctxA = modA.getModuleContext();
        ServiceReference<ServiceA> srefA = ctxA.getServiceReference(ServiceA.class);
        Assert.assertNotNull("ServiceReference not null", srefA);

        ServiceA srvA = ctxA.getService(srefA);
        Assert.assertEquals("ServiceA#1:ServiceA1#1:Hello", srvA.doStuff("Hello"));

        assertDisableComponent(ctxA, srvA);
    }

    private void assertDisableComponent(final ModuleContext ctxA, ServiceA srvA) throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);
        ServiceListener listener = new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent event) {
                int type = event.getType();
                ServiceReference<?> sref = event.getServiceReference();
                List<String> clazzes = Arrays.asList(((String[]) sref.getProperty(Constants.OBJECTCLASS)));
                if (type == ServiceEvent.UNREGISTERING && clazzes.contains(ServiceA1.class.getName())) {
                    ctxA.removeServiceListener(this);
                    latch.countDown();
                }
            }
        };
        ctxA.addServiceListener(listener);

        ComponentContext ccA1 = srvA.getServiceA1().getComponentContext();
        ccA1.disableComponent(ServiceA1.class.getName());

        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));

        ServiceReference<ServiceA> srefA = ctxA.getServiceReference(ServiceA.class);
        Assert.assertNull("ServiceReference null", srefA);
        ServiceReference<ServiceA1> srefA1 = ctxA.getServiceReference(ServiceA1.class);
        Assert.assertNull("ServiceReference null", srefA1);
    }
}
