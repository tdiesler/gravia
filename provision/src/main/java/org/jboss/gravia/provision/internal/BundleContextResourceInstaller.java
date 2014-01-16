package org.jboss.gravia.provision.internal;

import java.io.InputStream;
import java.util.Map;

import org.jboss.gravia.provision.DefaultResourceHandle;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.spi.AbstractResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BundleContextResourceInstaller extends AbstractResourceInstaller {

    static final Logger LOGGER = LoggerFactory.getLogger(BundleContextResourceInstaller.class);

    private final RuntimeEnvironment environment;
    private final BundleContext bundleContext;

    BundleContextResourceInstaller(BundleContext bundleContext, RuntimeEnvironment environment) {
        this.bundleContext = bundleContext;
        this.environment = environment;
    }

    @Override
    public RuntimeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public ResourceHandle installUnsharedResource(Resource resource, Map<Requirement, Resource> mapping) throws ProvisionException {
        LOGGER.info("Installing unshared resource: {}", resource);
        return installBundleResource(resource);
    }

    @Override
    public ResourceHandle installSharedResource(Resource resource, Map<Requirement, Resource> mapping) throws ProvisionException {
        LOGGER.info("Installing shared resource: {}", resource);
        return installBundleResource(resource);
    }

    private ResourceHandle installBundleResource(Resource resource) throws ProvisionException {
        ResourceIdentity identity = resource.getIdentity();
        InputStream content = resource.adapt(ResourceContent.class).getContent();
        final Bundle bundle;
        try {
            bundle = bundleContext.installBundle(identity.toString(), content);
        } catch (BundleException ex) {
            throw new ProvisionException(ex);
        }

        // Attempt to start the bundle. This relies on provision ordering.
        try {
            bundle.start();
        } catch (BundleException ex) {
            // ignore
        }

        // Install the bundle as module if it has not already happened
        // A bundle that could not get resolved will have no associated module
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(identity);
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
        // [TODO] Revisit {@link ModuleListener} handling for OSGi
        Resource envres = environment.getResource(identity);
        if (envres == null && module != null) {
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.assertRuntimeEnvironment(environment);
            runtimeEnv.addRuntimeResource(resource);
        }

        return new BundleResourceHandle(resource, module, bundle);
    }

    static class BundleResourceHandle extends DefaultResourceHandle {

        private final Bundle bundle;

        BundleResourceHandle(Resource resource, Module module, Bundle bundle) {
            super(resource, module);
            this.bundle = bundle;
        }

        Bundle getBundle() {
            return bundle;
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