/*
 * #%L
 * Wildfly Gravia Subsystem
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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


package org.wildfly.extension.gravia.service;

import java.io.File;
import java.util.Properties;

import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.embedded.EmbeddedRuntimeFactory;
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
        Runtime runtime = RuntimeLocator.createRuntime(new EmbeddedRuntimeFactory(), propertiesProvider);
        runtime.init();
    }

    @Override
    public Runtime getValue() throws IllegalStateException {
        return RuntimeLocator.getRuntime();
    }

    protected ServerEnvironment getServerEnvironment() {
        return injectedServerEnvironment.getValue();
    }

    protected Properties getRuntimeProperties() {

        Properties properties = new Properties();
        ServerEnvironment serverEnv = getServerEnvironment();
        File storageDir = new File(serverEnv.getServerDataDir().getPath() + File.separator + Constants.RUNTIME_STORAGE_DEFAULT);

        // Gravia integration properties
        properties.setProperty(Constants.RUNTIME_STORAGE_CLEAN, Constants.RUNTIME_STORAGE_CLEAN_ONFIRSTINIT);
        properties.setProperty(Constants.RUNTIME_STORAGE, storageDir.getAbsolutePath());

        return properties;
    }
}
