/*
 * #%L
 * JBossOSGi SPI
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

import java.io.InputStream;

import org.jboss.gravia.resource.Constants;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.tomcat.ApplicationActivator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.test.gravia.itests.ArchiveBuilder.TargetContainer;
import org.jboss.test.gravia.itests.sub.a.ServiceA;
import org.jboss.test.gravia.itests.sub.a1.ServiceA1;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jbos.com
 * @since 04-Oct-2013
 */
public abstract class ServiceComponentTest  {

    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("scr-test");
        archive.addClasses(ServiceComponentTest.class);
        archive.addWebClasses(ApplicationActivator.class);
        archive.addClasses(ServiceA.class, ServiceA1.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.a.ServiceA.xml");
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.a1.ServiceA1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (archive.getTargetContainer() == TargetContainer.karaf) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addImportPackages(RuntimeLocator.class, ComponentContext.class, Resource.class);
                    builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.itests.sub.a.ServiceA.xml,OSGI-INF/org.jboss.test.gravia.itests.sub.a1.ServiceA1.xml");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(archive.getName(), "1.0.0");
                    builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.itests.sub.a.ServiceA.xml,OSGI-INF/org.jboss.test.gravia.itests.sub.a1.ServiceA1.xml");
                    builder.addManifestHeader("Dependencies", "org.jboss.gravia");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Test
    public void testBasicModule() throws Exception {

        Runtime runtime = RuntimeLocator.getRuntime();
        Module modA = runtime.getModule(getClass().getClassLoader());

        ModuleContext ctxA = modA.getModuleContext();
        ServiceReference<ServiceA> srefA = ctxA.getServiceReference(ServiceA.class);
        Assert.assertNotNull("ServiceReference not null", srefA);

        ServiceA srvA = ctxA.getService(srefA);
        Assert.assertEquals("ServiceA#1:ServiceA1#1:Hello", srvA.doStuff("Hello"));
    }
}
