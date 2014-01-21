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
package org.jboss.gravia.provision;

import org.jboss.gravia.provision.spi.AbstractProvisioner;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.DefaultPreferencePolicy;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.PreferencePolicy;
import org.jboss.gravia.resolver.Resolver;

/**
 * The default {@link Provisioner}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public class DefaultProvisioner extends AbstractProvisioner {

    public DefaultProvisioner(Environment environment, Resolver resolver, Repository repository, ResourceInstaller installer) {
        super(environment, resolver, repository, installer, new DefaultPreferencePolicy(null));
    }

    public DefaultProvisioner(Environment environment, Resolver resolver, Repository repository, ResourceInstaller installer, PreferencePolicy policy) {
        super(environment, resolver, repository, installer, policy);
    }
}
