/*
 * #%L
 * Gravia :: Arquillian :: Container
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
package org.jboss.gravia.arquillian.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.openmbean.CompositeData;

import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.RepositoryMBean;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.utils.IOUtils;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.MBeanProxy;

/**
 * A task which is run for container setup.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Dec-2013
 */
public abstract class ContainerSetupTask {

    public interface Context {

        MBeanServerConnection getMBeanServer();

        Map<String, String> getConfiguration();
    }

    protected void setUp(Context context) throws Exception {
        // do nothing
    }

    protected void tearDown(Context context) throws Exception {
        // do nothing
    }

    protected Set<ResourceIdentity> addRepositoryContent(Context context, URL resurl) throws IOException {
        IllegalArgumentAssertion.assertNotNull(context, "context");
        IllegalArgumentAssertion.assertNotNull(resurl, "resurl");

        RepositoryMBean repository = MBeanProxy.get(context.getMBeanServer(), RepositoryMBean.OBJECT_NAME, RepositoryMBean.class);

        Set<ResourceIdentity> result = new HashSet<>();
        InputStream input = resurl.openStream();
        try {
            RepositoryReader reader = new DefaultRepositoryXMLReader(input);
            Resource auxres = reader.nextResource();
            while (auxres != null) {
                ResourceIdentity identity = auxres.getIdentity();
                if (repository.getResource(identity.getCanonicalForm()) == null) {
                    repository.addResource(auxres.adapt(CompositeData.class));
                    result.add(identity);
                }
                auxres = reader.nextResource();
            }
        } finally {
            IOUtils.safeClose(input);
        }
        return Collections.unmodifiableSet(result);
    }

    protected void removeRepositoryContent(Context context, Set<ResourceIdentity> identities) throws IOException {
        IllegalArgumentAssertion.assertNotNull(context, "context");
        IllegalArgumentAssertion.assertNotNull(identities, "identities");

        RepositoryMBean repository = MBeanProxy.get(context.getMBeanServer(), RepositoryMBean.OBJECT_NAME, RepositoryMBean.class);

        for (ResourceIdentity resid : identities) {
            repository.removeResource(resid.getCanonicalForm());
        }
    }
}
