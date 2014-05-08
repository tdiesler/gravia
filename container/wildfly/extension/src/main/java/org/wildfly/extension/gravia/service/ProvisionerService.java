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

import org.jboss.gravia.provision.Provisioner;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service providing the {@link Provisioner}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 27-Jun-2013
 */
public class ProvisionerService extends AbstractService<Provisioner> {

    private final Provisioner provisioner;

    public ProvisionerService(Provisioner provisioner) {
        this.provisioner = provisioner;
    }

    public ServiceController<Provisioner> install(ServiceTarget serviceTarget) {
        ServiceBuilder<Provisioner> builder = serviceTarget.addService(GraviaConstants.PROVISIONER_SERVICE_NAME, this);
        return builder.install();
    }

    @Override
    public Provisioner getValue() throws IllegalStateException {
        return provisioner;
    }
}
