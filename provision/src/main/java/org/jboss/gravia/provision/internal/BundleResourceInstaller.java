/*
 * #%L
 * Gravia :: Provision
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.provision.internal;

import static org.jboss.gravia.provision.spi.ProvisionLogger.LOGGER;

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.spi.AbstractResourceHandle;
import org.jboss.gravia.provision.spi.AbstractResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.spi.ThreadResourceAssociation;
import org.jboss.gravia.utils.IllegalStateAssertion;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;

public class BundleResourceInstaller extends AbstractResourceInstaller {

    private final RuntimeEnvironment environment;
    private final BundleContext context;

    BundleResourceInstaller(BundleContext context, RuntimeEnvironment environment) {
        this.context = context;
        this.environment = environment;
    }

    @Override
    public RuntimeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public ResourceHandle installResourceProtected(Context context, Resource resource) throws ProvisionException {
        LOGGER.info("Installing resource: {}", resource);
        return installBundleResource(resource);
    }

    private ResourceHandle installBundleResource(Resource resource) throws ProvisionException {

        // Install the Bundle
        ResourceIdentity resid = resource.getIdentity();
        ResourceContent content = getFirstRelevantResourceContent(resource);
        IllegalStateAssertion.assertNotNull(content.getContent(), "Cannot obtain content from: " + resource);

        Bundle bundle;
        try {
            String location = "resource#" + resid;
            bundle = context.installBundle(location, content.getContent());
        } catch (BundleException ex) {
            throw new ProvisionException(ex);
        }

        // Start the bundle. This relies on provision ordering.
        ThreadResourceAssociation.putResource(resource);
        try {
            bundle.start();
        } catch (BundleException ex) {
            // The start exception must be reported back to the client
            // WildFly also relies on provision ordering.
            throw new ProvisionException(ex);
        } finally {
            ThreadResourceAssociation.removeResource();
        }

        // Install the bundle as module if it has not already happened
        // A bundle that could not get resolved will not have an associated module
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(resid);
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (module == null && wiring != null) {
            try {
                ClassLoader classLoader = wiring.getClassLoader();
                module = runtime.installModule(classLoader, resource, null);
            } catch (ModuleException ex) {
                throw new ProvisionException(ex);
            }
        }

        // Installing a bundle does not trigger a {@link ModuleEvent#INSTALLED}
        // event because the Bundle's class loader is not (yet) available
        // We manually add the resource to the {@link RuntimeEnvironment}
        Resource envres = environment.getResource(resid);
        if (envres == null && module != null) {
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.assertRuntimeEnvironment(environment);
            runtimeEnv.addRuntimeResource(resource);
        }

        return new BundleResourceHandle(resource, module, bundle);
    }

    static class BundleResourceHandle extends AbstractResourceHandle {

        private final Bundle bundle;

        BundleResourceHandle(Resource resource, Module module, Bundle bundle) {
            super(module.adapt(Resource.class), module);
            this.bundle = bundle;
        }

        @Override
        public void uninstall() {
            try {
                bundle.uninstall();
            } catch (BundleException ex) {
                LOGGER.warn("Cannot uninstall bundle: " + bundle, ex);
            }
        }
    }
}
