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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultMatchPolicy;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.MatchPolicy;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceRegistration;
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
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service providing the {@link Environment}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 27-Jun-2013
 */
public class EnvironmentService extends AbstractService<Environment> {

    private final InjectedValue<ServerEnvironment> injectedServerEnvironment = new InjectedValue<ServerEnvironment>();
    private final InjectedValue<Runtime> injectedRuntime = new InjectedValue<Runtime>();
    private ServiceRegistration<?> registration;
    private RuntimeEnvironment environment;

    public ServiceController<Environment> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<Environment> builder = serviceTarget.addService(GraviaConstants.ENVIRONMENT_SERVICE_NAME, this);
        builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, injectedServerEnvironment);
        builder.addDependency(GraviaConstants.RUNTIME_SERVICE_NAME, Runtime.class, injectedRuntime);
        builder.addListener(verificationHandler);
        return builder.install();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void start(StartContext startContext) throws StartException {
        Runtime runtime = injectedRuntime.getValue();
        MatchPolicy matchPolicy = new DefaultMatchPolicy();
        File modulesDir = injectedServerEnvironment.getValue().getModulesDir();
        environment = new RuntimeEnvironment(runtime, new SystemResourceStore(modulesDir), matchPolicy);

        ModuleContext syscontext = runtime.getModuleContext();
        registration = syscontext.registerService(RuntimeEnvironment.class, environment, null);
    }

    @Override
    public void stop(StopContext context) {
        if (registration != null) {
            registration.unregister();
        }
    }

    @Override
    public Environment getValue() throws IllegalStateException {
        return environment;
    }

    static class SystemResourceStore implements ResourceStore {

        private final File modulesDir;

        SystemResourceStore(File modulesDir) {
            this.modulesDir = modulesDir;
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public MatchPolicy getMatchPolicy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Resource> getResources() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource addResource(Resource resource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource removeResource(ResourceIdentity identity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource getResource(ResourceIdentity identity) {
            String symbolicName = identity.getSymbolicName();
            Version version = identity.getVersion();
            String modname = symbolicName + (version != Version.emptyVersion ? ":" + version : "");
            ModuleIdentifier modid = ModuleIdentifier.fromString(modname);
            try {
                ModuleLoader moduleLoader = Module.getBootModuleLoader();
                moduleLoader.loadModule(modid);
            } catch (ModuleLoadException ex) {
                return null;
            }
            DefaultResourceBuilder builder = new DefaultResourceBuilder();
            Capability icap = builder.addIdentityCapability(symbolicName, version);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, IdentityNamespace.TYPE_ABSTRACT);
            return builder.getResource();
        }

        @Override
        public Set<Capability> findProviders(Requirement requirement) {
            String symbolicName = (String) requirement.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE);
            VersionRange versionRange = (VersionRange) requirement.getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);

            Set<Capability> result = new HashSet<Capability>();

            // Find the module versions that match
            File moduleDir = new File(modulesDir, symbolicName.replace(".", File.separator));
            if (versionRange != null && moduleDir.isDirectory()) {
                for (File file : moduleDir.listFiles()) {
                    if (!file.isDirectory() || file.getName().equals("main")) {
                        continue;
                    }
                    Version version;
                    try {
                        version = new Version(file.getName());
                    } catch (Throwable th) {
                        continue;
                    }
                    if (!versionRange.includes(version)) {
                        continue;
                    }
                    String modname = symbolicName + ":" + version;
                    ModuleIdentifier modid = ModuleIdentifier.fromString(modname);
                    try {
                        ModuleLoader moduleLoader = Module.getBootModuleLoader();
                        moduleLoader.loadModule(modid);
                    } catch (ModuleLoadException ex) {
                        continue;
                    }
                    DefaultResourceBuilder builder = new DefaultResourceBuilder();
                    Capability icap = builder.addIdentityCapability(symbolicName, version);
                    icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, IdentityNamespace.TYPE_ABSTRACT);
                    result.add(builder.getResource().getIdentityCapability());
                }
            }

            // Add the main module
            if (result.isEmpty()) {
                ModuleIdentifier modid = ModuleIdentifier.fromString(symbolicName);
                try {
                    ModuleLoader moduleLoader = Module.getBootModuleLoader();
                    moduleLoader.loadModule(modid);

                    DefaultResourceBuilder builder = new DefaultResourceBuilder();
                    Capability icap = builder.addIdentityCapability(symbolicName, Version.emptyVersion);
                    icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, IdentityNamespace.TYPE_ABSTRACT);
                    result.add(builder.getResource().getIdentityCapability());
                } catch (ModuleLoadException ex) {
                    // ignore
                }
            }
            return Collections.unmodifiableSet(result);
        }
    }
}
