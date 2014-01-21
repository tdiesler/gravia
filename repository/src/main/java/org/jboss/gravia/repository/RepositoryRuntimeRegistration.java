package org.jboss.gravia.repository;

import static org.jboss.gravia.repository.spi.RepositoryLogger.LOGGER;

import java.io.IOException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.jboss.gravia.resource.CompositeDataResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.utils.NotNullException;

/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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

/**
 * A {@link Repository} aggregator.
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Dec-2013
 */
public class RepositoryRuntimeRegistration {

    // Hide ctor
    private RepositoryRuntimeRegistration() {
    }

    public static Registration registerRepository(ModuleContext context, Repository repository) {
        NotNullException.assertValue(context, "context");
        NotNullException.assertValue(repository, "repository");

        // Register as runtime service
        final ServiceRegistration<Repository> sreg = context.registerService(Repository.class, repository, null);

        // Register as MBean
        ServiceReference<MBeanServer> sref = context.getServiceReference(MBeanServer.class);
        final MBeanServer mbeanServer = context.getService(sref);
        try {
            RepositoryWrapper delegate = new RepositoryWrapper(repository);
            StandardMBean mbean = new StandardMBean(delegate, RepositoryMBean.class);
            mbeanServer.registerMBean(mbean, RepositoryMBean.OBJECT_NAME);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot register repository MBean", ex);
        }

        return new Registration() {
            @Override
            public void unregister() {
                try {
                    mbeanServer.unregisterMBean(RepositoryMBean.OBJECT_NAME);
                } catch (Exception ex) {
                    LOGGER.error("Cannot unregister repository", ex);
                }
                sreg.unregister();
            }
        };
    }

    public interface Registration {
        void unregister();
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
