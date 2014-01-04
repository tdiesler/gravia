/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

    protected void setupRepositoryContent(MBeanServerConnection server, RepositoryMBean repository, Map<String, String> props) {
        for (String name : getInitialFeatureNames()) {
            String resname = "META-INF/repository-content/" + name + ".feature.xml";
            URL resurl = getClass().getClassLoader().getResource(resname);
            try {
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
            } catch (IOException e) {
                throw new IllegalStateException("Cannot install feature to repository: " + resname);
            }
        }
    }

    protected void removeRepositoryContent(MBeanServerConnection server, RepositoryMBean repository, Map<String, String> props) {
        for (String name : getInitialFeatureNames()) {
            String resname = "META-INF/repository-content/" + name + ".feature.xml";
            URL resurl = getClass().getClassLoader().getResource(resname);
            try {
                InputStream input = resurl.openStream();
                RepositoryReader reader = new DefaultRepositoryXMLReader(input);
                Resource auxres = reader.nextResource();
                while (auxres != null) {
                    String identity = auxres.getIdentity().toString();
                    repository.removeResource(identity);
                    auxres = reader.nextResource();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot remove feature from repository: " + resname);
            }
        }
    }
}
