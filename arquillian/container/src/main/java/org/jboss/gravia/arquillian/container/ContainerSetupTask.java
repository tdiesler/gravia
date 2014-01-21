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
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.openmbean.CompositeData;

import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.RepositoryMBean;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.utils.MBeanProxy;

/**
 * A task which is run for container setup.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Dec-2013
 */
public abstract class ContainerSetupTask {

    protected void setUp(MBeanServerConnection server, Map<String, String> props) throws Exception {
        if (server != null && server.isRegistered(RepositoryMBean.OBJECT_NAME)) {
            RepositoryMBean repository = MBeanProxy.get(server, RepositoryMBean.OBJECT_NAME, RepositoryMBean.class);
            setupRepositoryContent(server, repository, props);
        }
    }

    protected void tearDown(MBeanServerConnection server, Map<String, String> props) throws Exception {
        if (server != null && server.isRegistered(RepositoryMBean.OBJECT_NAME)) {
            RepositoryMBean repository = MBeanProxy.get(server, RepositoryMBean.OBJECT_NAME, RepositoryMBean.class);
            removeRepositoryContent(server, repository, props);
        }
    }

    protected String[] getInitialFeatureNames() {
        return new String[0];
    }

    protected void setupRepositoryContent(MBeanServerConnection server, RepositoryMBean repository, Map<String, String> props) throws IOException {
        for (String name : getInitialFeatureNames()) {
            String resname = "META-INF/repository-content/" + name + ".feature.xml";
            URL resurl = getClass().getClassLoader().getResource(resname);
            InputStream input = resurl.openStream();
            RepositoryReader reader = new DefaultRepositoryXMLReader(input);
            Resource auxres = reader.nextResource();
            while (auxres != null) {
                String identity = auxres.getIdentity().toString();
                if (repository.getResource(identity) == null) {
                    repository.addResource(auxres.adapt(CompositeData.class));
                }
                auxres = reader.nextResource();
            }
        }
    }

    protected void removeRepositoryContent(MBeanServerConnection server, RepositoryMBean repository, Map<String, String> props) throws IOException {
        for (String name : getInitialFeatureNames()) {
            String resname = "META-INF/repository-content/" + name + ".feature.xml";
            URL resurl = getClass().getClassLoader().getResource(resname);
            InputStream input = resurl.openStream();
            RepositoryReader reader = new DefaultRepositoryXMLReader(input);
            Resource auxres = reader.nextResource();
            while (auxres != null) {
                String identity = auxres.getIdentity().toString();
                repository.removeResource(identity);
                auxres = reader.nextResource();
            }
        }
    }
}
