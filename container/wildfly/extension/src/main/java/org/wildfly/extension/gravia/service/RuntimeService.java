/*
 * #%L
 * Gravia :: Container :: WildFly :: Extension
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
package org.wildfly.extension.gravia.service;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.gravia.Constants;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.spi.DefaultPropertiesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service providing the {@link Runtime}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 08-Oct-2013
 */
public class RuntimeService extends AbstractService<Runtime> {

    private final InjectedValue<ServerEnvironment> injectedServerEnvironment = new InjectedValue<ServerEnvironment>();
    private Runtime runtime;

    public ServiceController<Runtime> install(ServiceTarget serviceTarget) {
        ServiceBuilder<Runtime> builder = serviceTarget.addService(GraviaConstants.RUNTIME_SERVICE_NAME, this);
        builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, injectedServerEnvironment);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        PropertiesProvider propsProvider = new DefaultPropertiesProvider(initialProperties(), true);
        runtime = RuntimeLocator.createRuntime(new WildFlyRuntimeFactory(), propsProvider);
        runtime.init();
    }

    @Override
    public void stop(StopContext context) {
        try {
            runtime.shutdown();
            runtime.awaitShutdown(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            // ignore
        } finally {
            RuntimeLocator.releaseRuntime();
        }
    }

    @Override
    public Runtime getValue() throws IllegalStateException {
        return runtime;
    }

    protected ServerEnvironment getServerEnvironment() {
        return injectedServerEnvironment.getValue();
    }

    protected Properties initialProperties() {

        Properties properties = new Properties();
        ServerEnvironment serverEnv = getServerEnvironment();
        File storageDir = new File(serverEnv.getServerDataDir(), Constants.RUNTIME_STORAGE_DEFAULT);
        File configsDir = new File(serverEnv.getServerConfigurationDir(), "gravia" + File.separator + "configs");
        File repositoryDir = new File(serverEnv.getServerDataDir().getPath() + File.separator + "repository");

        // Gravia integration properties
        properties.setProperty(Constants.RUNTIME_STORAGE_DIR, storageDir.getAbsolutePath());
        properties.setProperty(Constants.RUNTIME_CONFIGURATIONS_DIR, configsDir.getAbsolutePath());
        properties.setProperty(Constants.PROPERTY_REPOSITORY_STORAGE_DIR, repositoryDir.getAbsolutePath());
        properties.setProperty(Constants.RUNTIME_TYPE, RuntimeType.WILDFLY.toString());

        return properties;
    }
}
