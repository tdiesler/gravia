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

import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
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

    public ServiceController<Runtime> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<Runtime> builder = serviceTarget.addService(GraviaConstants.RUNTIME_SERVICE_NAME, this);
        builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, injectedServerEnvironment);
        builder.addListener(verificationHandler);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        PropertiesProvider propertiesProvider = new DefaultPropertiesProvider(getRuntimeProperties(), true);
        Runtime runtime = RuntimeLocator.createRuntime(new WildFlyRuntimeFactory(), propertiesProvider);
        runtime.init();
    }

    @Override
    public Runtime getValue() throws IllegalStateException {
        return RuntimeLocator.getRequiredRuntime();
    }

    protected ServerEnvironment getServerEnvironment() {
        return injectedServerEnvironment.getValue();
    }

    protected Properties getRuntimeProperties() {

        Properties properties = new Properties();
        ServerEnvironment serverEnv = getServerEnvironment();
        File storageDir = new File(serverEnv.getServerDataDir().getPath() + File.separator + org.jboss.gravia.Constants.RUNTIME_STORAGE_DEFAULT);

        // Gravia integration properties
        properties.setProperty(org.jboss.gravia.Constants.RUNTIME_STORAGE_CLEAN, org.jboss.gravia.Constants.RUNTIME_STORAGE_CLEAN_ONFIRSTINIT);
        properties.setProperty(org.jboss.gravia.Constants.RUNTIME_STORAGE, storageDir.getAbsolutePath());
        properties.setProperty(org.jboss.gravia.Constants.RUNTIME_TYPE, "wildfly");

        return properties;
    }
}
