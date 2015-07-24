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

import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
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
 * Service providing the system {@link ModuleContext}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-Nov-2013
 */
public class ModuleContextService extends AbstractService<ModuleContext> {

    private final InjectedValue<Runtime> injectedRuntime = new InjectedValue<Runtime>();

    private ModuleContext syscontext;

    public ServiceController<ModuleContext> install(ServiceTarget serviceTarget) {
        ServiceBuilder<ModuleContext> builder = serviceTarget.addService(GraviaConstants.MODULE_CONTEXT_SERVICE_NAME, this);
        builder.addDependency(GraviaConstants.RUNTIME_SERVICE_NAME, Runtime.class, injectedRuntime);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        Runtime runtime = injectedRuntime.getValue();
        syscontext = runtime.getModuleContext();
    }

    @Override
    public void stop(StopContext context) {
        syscontext = null;
    }

    @Override
    public ModuleContext getValue() throws IllegalStateException {
        return syscontext;
    }
}
