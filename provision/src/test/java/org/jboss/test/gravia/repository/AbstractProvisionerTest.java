/*
 * #%L
 * JBossOSGi Provision: Core
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.repository;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.stream.XMLStreamException;

import org.jboss.gravia.provision.DefaultEnvironment;
import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ProvisionResult;
import org.jboss.gravia.provision.Provisioner;
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
    Provisioner provisionService;
    Environment environment;

    @Before
    public void setUp() throws Exception {
        File storageDir = new File("./target/repository/" + System.currentTimeMillis()).getCanonicalFile();
        environment = new DefaultEnvironment("TestEnv");
        Resolver resolver = new DefaultResolver();
        ConfigurationPropertyProvider propertyProvider = Mockito.mock(ConfigurationPropertyProvider.class);
        Mockito.when(propertyProvider.getProperty(Repository.PROPERTY_REPOSITORY_STORAGE_DIR, null)).thenReturn(storageDir.getPath());
        repository = new DefaultPersistentRepository(propertyProvider, new DefaultMavenIdentityRepository(propertyProvider));
        provisionService = new DefaultProvisioner(resolver, repository);
    }

    Provisioner getProvisioner() {
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
