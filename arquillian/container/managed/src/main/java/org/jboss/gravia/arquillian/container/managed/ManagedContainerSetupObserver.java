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
package org.jboss.gravia.arquillian.container.managed;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.context.ObjectStore;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.gravia.arquillian.container.SetupObserver;
import org.jboss.gravia.arquillian.container.managed.ManagedSetupTask.ManagedContext;

/**
 * An Arquillian container setup observer.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Dec-2013
 */
public class ManagedContainerSetupObserver extends SetupObserver<ManagedSetupTask> {

    public static final String PROPERTY_JMX_SERVICE_URL = "jmxServiceURL";
    public static final String PROPERTY_JMX_USERNAME = "jmxUsername";
    public static final String PROPERTY_JMX_PASSWORD = "jmxPassword";

    @Inject
    private Instance<SuiteContext> suiteContextInstance;

    @Inject
    private Instance<ClassContext> classContextInstance;

    @Inject
    @ApplicationScoped
    private InstanceProducer<MBeanServerConnection> mbeanServerInstance;

    @Override
    @SuppressWarnings("unchecked")
    protected ManagedSetupContext getSetupContext(ObjectStore suiteStore, ObjectStore classStore) {
        MBeanServerConnection server = mbeanServerInstance.get();
        return new ManagedSetupContext(suiteStore, classStore, server, null);
    }

    public void handleAfterStart(@Observes AfterStart event, Container container) throws Throwable {

        // Provide {@link MBeanServerConnection} and {@link RepositoryMBean}
        MBeanServerConnection mbeanServer = getMBeanServerConnection(container);
        if (mbeanServer != null) {
            mbeanServerInstance.set(mbeanServer);
        }
    }

    public void handleBeforeDeploy(@Observes BeforeDeploy event, Container container) throws Throwable {
        List<ManagedSetupTask> setupTasks = getSetupTasks();
        if (!setupTasks.isEmpty()) {
            ClassContext classContext = classContextInstance.get();
            ObjectStore suiteStore = suiteContextInstance.get().getObjectStore();
            ObjectStore classStore = classContext.getObjectStore();
            MBeanServerConnection server = mbeanServerInstance.get();
            Map<String, String> props = container.getContainerConfiguration().getContainerProperties();
            ManagedSetupContext context = new ManagedSetupContext(suiteStore, classStore, server, props);
            for (ManagedSetupTask task : setupTasks) {
                task.beforeDeploy(context);
            }
        }
    }

    public void handleAfterDeploy(@Observes AfterDeploy event, Container container) throws Throwable {
    }

    public void handleBeforeStop(@Observes BeforeStop event, Container container) throws Throwable {
        List<ManagedSetupTask> setupTasks = getSetupTasks();
        if (!setupTasks.isEmpty()) {
            ObjectStore suiteStore = suiteContextInstance.get().getObjectStore();
            MBeanServerConnection server = mbeanServerInstance.get();
            Map<String, String> props = container.getContainerConfiguration().getContainerProperties();
            ManagedSetupContext context = new ManagedSetupContext(suiteStore, null, server, props);
            for (ManagedSetupTask task : setupTasks) {
                task.beforeStop(context);
            }
        }
    }

    public void handleAfterStop(@Observes AfterStop event, Container container) throws Throwable {
    }

    private MBeanServerConnection getMBeanServerConnection(Container container) throws IOException {

        Map<String, String> props = container.getContainerConfiguration().getContainerProperties();
        String jmxServiceURL = props.get(PROPERTY_JMX_SERVICE_URL);
        String jmxUsername = props.get(PROPERTY_JMX_USERNAME);
        String jmxPassword = props.get(PROPERTY_JMX_PASSWORD);

        MBeanServerConnection mbeanServer = null;
        try {
            JMXServiceURL serviceURL = new JMXServiceURL(jmxServiceURL);
            Map<String, Object> env = new HashMap<String, Object>();
            if (jmxUsername != null && jmxPassword != null) {
                String[] credentials = new String[] { jmxUsername, jmxPassword };
                env.put(JMXConnector.CREDENTIALS, credentials);
            }
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, env);
            mbeanServer = connector.getMBeanServerConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mbeanServer;
    }

    private final static class ManagedSetupContext extends AbstractSetupContext implements ManagedContext {
        private final MBeanServerConnection server;
        private final Map<String, String> configuration;

        ManagedSetupContext(ObjectStore suiteStore, ObjectStore classStore, MBeanServerConnection server, Map<String, String> configuration) {
            super(suiteStore, classStore);
            this.server = server;
            this.configuration = configuration;
        }

        public MBeanServerConnection getMBeanServer() {
            return server;
        }

        public Map<String, String> getConfiguration() {
            return Collections.unmodifiableMap(configuration);
        }
    }
}