/*
 * #%L
 * Gravia :: Resolver
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
package org.jboss.gravia.repository.internal;

import java.io.IOException;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryMBean;
import org.jboss.gravia.resource.CompositeDataResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RepositoryMBean} component.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-May-2014
 */
@Component(configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public final class RepositoryMBeanService {

    private Repository repository;
    private MBeanServer mbeanServer;

    @Activate
    void activate(BundleContext context) throws JMException {
        StandardMBean mbean = new StandardMBean(new RepositoryWrapper(repository), RepositoryMBean.class);
        mbeanServer.registerMBean(mbean, RepositoryMBean.OBJECT_NAME);
    }

    @Deactivate
    void deactivate() throws JMException {
        mbeanServer.unregisterMBean(RepositoryMBean.OBJECT_NAME);
    }

    @Reference
    void bindMBeanServer(MBeanServer service) {
        mbeanServer = service;
    }
    void unbindMBeanServer(MBeanServer service) {
        mbeanServer = null;
    }

    @Reference
    void bindRepository(Repository service) {
        repository = service;
    }
    void unbindRepository(Repository service) {
        repository = null;
    }

    static class RepositoryWrapper implements RepositoryMBean {

        private final Repository repository;

        RepositoryWrapper(Repository repository) {
            this.repository = repository;
        }

        @Override
        public String getName() {
            return repository.getName();
        }

        @Override
        public TabularData findProviders(String namespace, String nsvalue, Map<String, Object> attributes, Map<String, String> directives) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompositeData addResource(CompositeData resData) throws IOException {
            Resource resource = new CompositeDataResourceBuilder(resData).getResource();
            return repository.addResource(resource).adapt(CompositeData.class);
        }

        @Override
        public CompositeData removeResource(String identity) {
            ResourceIdentity resid = ResourceIdentity.fromString(identity);
            Resource resource = repository.removeResource(resid);
            return resource != null ? resource.adapt(CompositeData.class) : null;
        }

        @Override
        public CompositeData getResource(String identity) {
            ResourceIdentity resid = ResourceIdentity.fromString(identity);
            Resource resource = repository.getResource(resid);
            return resource != null ? resource.adapt(CompositeData.class) : null;
        }
    }
}
