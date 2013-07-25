/*
 * #%L
 * JBossOSGi Provision: Core
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.repository;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.stream.XMLStreamException;

import org.jboss.gravia.provision.DefaultEnvironment;
import org.jboss.gravia.provision.DefaultResourceProvisioner;
import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ProvisionResult;
import org.jboss.gravia.provision.ResourceProvisioner;
import org.jboss.gravia.repository.DefaultMavenIdentityRepository;
import org.jboss.gravia.repository.DefaultPersistentRepository;
import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.PersistentRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.Repository.ConfigurationPropertyProvider;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.junit.Before;
import org.mockito.Mockito;

/**
 * The abstract provisioner test.
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public abstract class AbstractProvisionerTest {

    AtomicLong installIndex = new AtomicLong();
    PersistentRepository repository;
    ResourceProvisioner provisionService;
    Environment environment;

    @Before
    public void setUp() throws Exception {
        File storageDir = new File("./target/repository/" + System.currentTimeMillis()).getCanonicalFile();
        environment = new DefaultEnvironment("TestEnv");
        Resolver resolver = new DefaultResolver();
        ConfigurationPropertyProvider propertyProvider = Mockito.mock(ConfigurationPropertyProvider.class);
        Mockito.when(propertyProvider.getProperty(Repository.PROPERTY_REPOSITORY_STORAGE_DIR, null)).thenReturn(storageDir.getPath());
        repository = new DefaultPersistentRepository(propertyProvider, new DefaultMavenIdentityRepository(propertyProvider));
        provisionService = new DefaultResourceProvisioner(resolver, repository);
    }

    ResourceProvisioner getProvisioner() {
        return provisionService;
    }

    Environment getEnvironment() {
        return environment;
    }

    PersistentRepository getRepository() {
        return repository;
    }

    ProvisionResult findResources(Set<Requirement> reqs) {
        return getProvisioner().findResources(getEnvironment(), reqs);
    }

    void installResources(List<Resource> resources) throws ProvisionException {
        for (Resource res : resources) {
            environment.addResource(res);
        }
    }

    void setupRepository(String config) throws XMLStreamException {
        RepositoryStorage storage = getRepository().adapt(RepositoryStorage.class);
        RepositoryReader reader = getRepositoryReader(config);
        Resource res = reader.nextResource();
        while (res != null) {
            storage.addResource(res);
            res = reader.nextResource();
        }
    }

    RepositoryReader getRepositoryReader(String config) throws XMLStreamException {
        InputStream input = getClass().getClassLoader().getResourceAsStream(config);
        return new DefaultRepositoryXMLReader(input);
    }
}
