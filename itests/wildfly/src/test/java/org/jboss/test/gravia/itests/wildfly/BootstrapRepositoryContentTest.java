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
package org.jboss.test.gravia.itests.wildfly;

import java.io.InputStream;
import java.util.Collection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test initial {@link Repository}
 *
 * @author thomas.diesler@jboss.com
 * @since 19-Dec-2013
 */
@RunWith(Arquillian.class)
public class BootstrapRepositoryContentTest {

    private Repository repository;

    @Deployment
    public static Archive<?> deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "bootstrap-repository.war");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                ManifestBuilder builder = new ManifestBuilder();
                builder.addIdentityCapability(archive.getName(), "1.0.0");
                builder.addManifestHeader("Dependencies", "org.jboss.gravia");
                return builder.openStream();
            }
        });
        return archive;
    }

    @Before
    public void setUp() throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        ModuleContext syscontext = runtime.getModule(0).getModuleContext();
        ServiceReference<Repository> sref = syscontext.getServiceReference(Repository.class);
        repository = syscontext.getService(sref);
    }

    @Test
    public void testRepositoryContent() throws Exception {
        Assert.assertNotNull("Repository not null", repository);
        Requirement freq = new IdentityRequirementBuilder("camel.core.feature", (String)null).getRequirement();
        Collection<Capability> providers = repository.findProviders(freq);
        Assert.assertEquals("One provider", 1, providers.size());
    }
}
