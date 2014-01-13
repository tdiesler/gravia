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
package org.jboss.test.gravia.provision;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.gravia.Constants;
import org.jboss.gravia.provision.DefaultEnvironment;
import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ProvisionResult;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.repository.DefaultRepository;
import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
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
    Repository repository;
    Provisioner provisioner;
    Environment environment;

    @Before
    public void setUp() throws Exception {
        File storageDir = new File("./target/repository/" + System.currentTimeMillis()).getCanonicalFile();
        environment = new DefaultEnvironment("TestEnv");
        Resolver resolver = new DefaultResolver();
        PropertiesProvider propertyProvider = Mockito.mock(PropertiesProvider.class);
        Mockito.when(propertyProvider.getProperty(Constants.PROPERTY_REPOSITORY_STORAGE_DIR, null)).thenReturn(storageDir.getPath());
        repository = new DefaultRepository(propertyProvider);
        ResourceInstaller installer = Mockito.mock(ResourceInstaller.class);
        provisioner = new DefaultProvisioner(environment, resolver, repository, installer);
    }

    Provisioner getProvisioner() {
        return provisioner;
    }

    Environment getEnvironment() {
        return environment;
    }

    Repository getRepository() {
        return repository;
    }

    ProvisionResult findResources(Set<Requirement> reqs) {
        return getProvisioner().findResources(reqs);
    }

    void installResources(List<Resource> resources) throws ProvisionException {
        for (Resource res : resources) {
            environment.addResource(res);
        }
    }

    void setupRepository(String config) throws IOException {
        RepositoryReader reader = getRepositoryReader(config);
        Resource res = reader.nextResource();
        while (res != null) {
            getRepository().addResource(res);
            res = reader.nextResource();
        }
    }

    RepositoryReader getRepositoryReader(String config) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(config);
        return new DefaultRepositoryXMLReader(input);
    }
}
