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
package org.jboss.test.gravia.itests.wildfly;

import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.gravia.itests.ConfigurationAdminTest;
import org.jboss.test.gravia.itests.sub.ApplicationActivator;
import org.jboss.test.gravia.itests.sub.SimpleServlet;
import org.jboss.test.gravia.itests.sub.d.ServiceD;
import org.jboss.test.gravia.itests.sub.d1.ServiceD1;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.runner.RunWith;

/**
 * Test webapp deployemnts
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Oct-2013
 */
@RunWith(Arquillian.class)
public class WildFlyConfigurationAdminTestCase extends ConfigurationAdminTest {

    @Deployment
    public static WebArchive deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "configadmin-test.war");
        archive.addClasses(HttpRequest.class, ApplicationActivator.class, SimpleServlet.class, ConfigurationAdminTest.class);
        archive.addClasses(ServiceD.class, ServiceD1.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.d.ServiceD.xml");
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.d1.ServiceD1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                ManifestBuilder builder = new ManifestBuilder();
                builder.addManifestHeader(ManifestBuilder.GRAVIA_IDENTITY_CAPABILITY, "configadmin-test;version=1.0.0");
                builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.itests.sub.d.ServiceD.xml,OSGI-INF/org.jboss.test.gravia.itests.sub.d1.ServiceD1.xml");
                builder.addManifestHeader("Dependencies", "org.jboss.gravia");
                return builder.openStream();
            }
        });
        return archive;
    }
}
