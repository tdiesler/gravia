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
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.arquillian.container.ContainerSetup;
import org.jboss.gravia.arquillian.container.ContainerSetupTask;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.test.gravia.itests.support.ArchiveBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test initial {@link Repository} content.
 *
 * @author thomas.diesler@jboss.com
 * @since 19-Dec-2013
 */
@RunWith(Arquillian.class)
@ContainerSetup(RepositoryServiceTest.Setup.class)
public class RepositoryServiceTest {

    public static class Setup extends ContainerSetupTask {

        Set<ResourceIdentity> identities;

        @Override
        protected void setUp(Context context) throws Exception {
            String resname = "META-INF/repository-content/camel.core.feature.xml";
            URL resurl = getClass().getClassLoader().getResource(resname);
            identities = addRepositoryContent(context, resurl);
        }

        @Override
        protected void tearDown(Context context) throws Exception {
            removeRepositoryContent(context, identities);
        }
    }

    @Deployment
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("repository-service");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(Runtime.class, Resource.class, Repository.class);
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
    public void testRepositoryContent() throws Exception {
        Requirement freq = new IdentityRequirementBuilder("camel.core.feature", (String) null).getRequirement();
        Repository repository = ServiceLocator.getRequiredService(Repository.class);
        Collection<Capability> providers = repository.findProviders(freq);
        Assert.assertEquals("One provider", 1, providers.size());
    }
}
