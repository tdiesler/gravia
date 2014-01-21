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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.gravia.repository.RepositoryMBean;
import org.jboss.gravia.utils.MBeanProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Arquillian container setup observer.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Dec-2013
 */
public class ContainerSetupObserver {

    public static final String PROPERTY_JMX_SERVICE_URL = "jmxServiceURL";
    public static final String PROPERTY_JMX_USERNAME = "jmxUsername";
    public static final String PROPERTY_JMX_PASSWORD = "jmxPassword";

    static final Logger LOGGER = LoggerFactory.getLogger(ContainerSetupObserver.class);

    @Inject
    private Instance<ClassContext> classContextInstance;

    @Inject
    @ApplicationScoped
    private InstanceProducer<MBeanServerConnection> mbeanServerInstance;

    @Inject
    @ApplicationScoped
    private InstanceProducer<RepositoryMBean> repositoryInstance;

    private List<ContainerSetupTask> setupTasks;

    public void handleAfterStart(@Observes AfterStart event, Container container) throws Throwable {

        // Provide {@link MBeanServerConnection} and {@link RepositoryMBean}
        MBeanServerConnection mbeanServer = getMBeanServerConnection(container);
        if (mbeanServer != null) {
            mbeanServerInstance.set(mbeanServer);
            if (mbeanServer.isRegistered(RepositoryMBean.OBJECT_NAME)) {
                RepositoryMBean repository = MBeanProxy.get(mbeanServer, RepositoryMBean.OBJECT_NAME, RepositoryMBean.class);
                repositoryInstance.set(repository);
            }
        }
    }

    public void handleBeforeDeploy(@Observes BeforeDeploy event, Container container) throws Throwable {

        ClassContext classContext = classContextInstance.get();
        Class<?> currentClass = classContext.getActiveId();
        ContainerSetup setup = currentClass.getAnnotation(ContainerSetup.class);
        if (setup == null || setupTasks != null)
            return;

        // Call {@link ContainerSetupTask} array
        setupTasks = new ArrayList<ContainerSetupTask>();
        Class<? extends ContainerSetupTask>[] classes = setup.value();
        for (Class<? extends ContainerSetupTask> clazz : classes) {
            setupTasks.add(clazz.newInstance());
        }

        MBeanServerConnection server = mbeanServerInstance.get();
        Map<String, String> props = container.getContainerConfiguration().getContainerProperties();
        for (ContainerSetupTask task : setupTasks) {
            task.setUp(server, props);
        }
    }

    public void handleBeforeStop(@Observes BeforeStop event, Container container) throws Throwable {

        if (setupTasks != null) {
            Collections.reverse(setupTasks);

            MBeanServerConnection server = mbeanServerInstance.get();
            Map<String, String> props = container.getContainerConfiguration().getContainerProperties();
            for (ContainerSetupTask task : setupTasks) {
                task.tearDown(server, props);
            }
        }
    }

    private MBeanServerConnection getMBeanServerConnection(Container container) throws IOException {

        Map<String, String> props = container.getContainerConfiguration().getContainerProperties();
        String jmxServiceURL = props.get(PROPERTY_JMX_SERVICE_URL);
        String jmxUsername = props.get(PROPERTY_JMX_USERNAME);
        String jmxPassword = props.get(PROPERTY_JMX_PASSWORD);

        MBeanServerConnection mbeanServer = null;
        if (jmxServiceURL == null) {
            List<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
            if (serverArr.size() > 1)
                LOGGER.warn("Multiple MBeanServer instances: {}",  serverArr);

            if (serverArr.size() > 0) {
                mbeanServer = serverArr.get(0);
                LOGGER.debug("Found MBeanServer: {}", mbeanServer.getDefaultDomain());
            }
        } else {
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
                LOGGER.warn("Cannot create JMXServiceURL from: {}", jmxServiceURL);
            }
        }
        return mbeanServer;
    }
}
