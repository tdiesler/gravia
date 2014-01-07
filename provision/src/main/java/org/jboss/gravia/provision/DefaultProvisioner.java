/*
 * #%L
 * JBossOSGi Provision: Core
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.gravia.provision;

import java.util.Iterator;
import java.util.Set;

import org.jboss.gravia.provision.spi.AbstractProvisioner;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.DefaultPreferencePolicy;
import org.jboss.gravia.resolver.PreferencePolicy;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;

/**
 * The default {@link Provisioner}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public class DefaultProvisioner extends AbstractProvisioner {

    public DefaultProvisioner(Environment environment, Resolver resolver, Repository repository) {
        super(environment, resolver, repository, new DefaultPreferencePolicy(null));
    }

    public DefaultProvisioner(Environment environment, Resolver resolver, Repository repository, PreferencePolicy policy) {
        super(environment, resolver, repository, policy);
    }

    @Override
    public Set<ResourceHandle> provisionResources(Set<Requirement> reqs) throws ProvisionException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Environment cloneEnvironment(Environment env) {
        Environment clone = new DefaultEnvironment("Cloned " + env.getName());
        Iterator<Resource> itres = env.getResources();
        while (itres.hasNext()) {
            clone.addResource(itres.next());
        }
        return clone;
    }
}
