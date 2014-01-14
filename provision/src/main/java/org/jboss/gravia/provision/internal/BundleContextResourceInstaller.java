package org.jboss.gravia.provision.internal;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.provision.DefaultResourceHandle;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.spi.AbstractResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
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
    public synchronized Set<ResourceHandle> installResources(List<Resource> resources, Map<Requirement, Resource> mapping) throws ProvisionException {

        // Install the resources
        Set<ResourceHandle> handles = super.installResources(resources, mapping);

        // Start the installed bundles
        BundleException startException = null;
        for (ResourceHandle handle : handles) {
            Bundle bundle = ((BundleResourceHandle) handle).getBundle();
            try {
                bundle.start();
            } catch (BundleException ex) {
                startException = ex;
                break;
            }
        }

        // Uninstall resources and throw the start exception
        if (startException != null) {
            for (ResourceHandle handle : handles) {
                handle.uninstall();
            }
            throw new ProvisionException(startException);
        }

        return handles;
    }

    @Override
    public synchronized ResourceHandle installResource(Resource res, Map<Requirement, Resource> mapping) throws ProvisionException {
        ResourceHandle handle = super.installResource(res, mapping);
        Bundle bundle = ((BundleResourceHandle) handle).getBundle();
        try {
            bundle.start();
        } catch (BundleException ex) {
            handle.uninstall();
            throw new ProvisionException(ex);
        }
        return handle;
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
        return new BundleResourceHandle(resource, bundle);
    }

    static class BundleResourceHandle extends DefaultResourceHandle {

        private final Bundle bundle;

        BundleResourceHandle(Resource resource, Bundle bundle) {
            super(resource);
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