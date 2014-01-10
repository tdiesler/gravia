package org.jboss.gravia.repository;

import static org.jboss.gravia.repository.spi.AbstractRepository.LOGGER;

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
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.utils.NotNullException;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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

    public static Registration registerRepository(Runtime runtime, Repository repository) {
        NotNullException.assertValue(runtime, "runtime");
        NotNullException.assertValue(repository, "repository");

        // Register as runtime service
        ModuleContext syscontext = runtime.getModuleContext();
        final ServiceRegistration<Repository> sreg = syscontext.registerService(Repository.class, repository, null);

        // Register as MBean
        ServiceReference<MBeanServer> sref = syscontext.getServiceReference(MBeanServer.class);
        final MBeanServer mbeanServer = syscontext.getService(sref);
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
