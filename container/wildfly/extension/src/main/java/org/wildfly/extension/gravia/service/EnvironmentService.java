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

import java.util.Collections;
import java.util.Set;

import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service providing the {@link Environment}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 27-Jun-2013
 */
public class EnvironmentService extends AbstractService<Environment> {

    private final InjectedValue<Runtime> injectedRuntime = new InjectedValue<Runtime>();
    private Environment environment;

    public ServiceController<Environment> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<Environment> builder = serviceTarget.addService(GraviaConstants.ENVIRONMENT_SERVICE_NAME, this);
        builder.addDependency(GraviaConstants.RUNTIME_SERVICE_NAME, Runtime.class, injectedRuntime);
        builder.addListener(verificationHandler);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {

        Runtime runtime = injectedRuntime.getValue();
        environment = new RuntimeEnvironment(runtime){
            @Override
            public Set<Capability> findProviders(Requirement req) {
                Set<Capability> providers = super.findProviders(req);
                if (providers.isEmpty() && req.getNamespace().equals(IdentityNamespace.IDENTITY_NAMESPACE)) {
                    providers = findModuleProviders(req);
                }
                return providers;
            }

            @Override
            public Resource getResource(ResourceIdentity resid) {
                Resource resource = super.getResource(resid);
                if (resource == null) {
                    resource = getModuleResource(resid);
                }
                return resource;
            }
        };
    }

    @Override
    public Environment getValue() throws IllegalStateException {
        return environment;
    }

    static Set<Capability> findModuleProviders(Requirement req) {

        String sname = (String) req.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE);
        ModuleIdentifier modid = ModuleIdentifier.fromString(sname);
        try {
            ModuleLoader moduleLoader = Module.getBootModuleLoader();
            moduleLoader.loadModule(modid);
        } catch (ModuleLoadException ex) {
            return Collections.emptySet();
        }

        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        Capability icap = builder.addIdentityCapability(sname, Version.emptyVersion);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, IdentityNamespace.TYPE_ABSTRACT);
        builder.getResource();

        return Collections.singleton(icap);
    }

    static Resource getModuleResource(ResourceIdentity resid) {

        String sname = resid.getSymbolicName();
        if (!Version.emptyVersion.equals(resid.getVersion()))
            return null;

        ModuleIdentifier modid = ModuleIdentifier.fromString(sname);
        try {
            ModuleLoader moduleLoader = Module.getBootModuleLoader();
            moduleLoader.loadModule(modid);
        } catch (ModuleLoadException ex) {
            return null;
        }

        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        Capability icap = builder.addIdentityCapability(sname, Version.emptyVersion);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, IdentityNamespace.TYPE_ABSTRACT);
        return builder.getResource();
    }
}
