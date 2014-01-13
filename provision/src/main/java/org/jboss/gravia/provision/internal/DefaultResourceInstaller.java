package org.jboss.gravia.provision.internal;

import java.util.Map;

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.spi.AbstractResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;

class DefaultResourceInstaller extends AbstractResourceInstaller {

    private final RuntimeEnvironment environment;

    DefaultResourceInstaller(RuntimeEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public RuntimeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public ResourceHandle installUnsharedResource(Resource res, Map<Requirement, Resource> mapping) throws ProvisionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceHandle installSharedResource(Resource res, Map<Requirement, Resource> mapping) throws ProvisionException {
        throw new UnsupportedOperationException();
    }
}