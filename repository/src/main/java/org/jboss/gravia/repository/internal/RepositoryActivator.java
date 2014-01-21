package org.jboss.gravia.repository.internal;

/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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

import org.jboss.gravia.repository.DefaultRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration.Registration;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.spi.RuntimePropertiesProvider;

/**
 * Activate the {@link Repository} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class RepositoryActivator implements ModuleActivator {

    private Registration registration;

    @Override
    public void start(ModuleContext context) throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        DefaultRepository repository = new DefaultRepository(new RuntimePropertiesProvider(runtime));
        registration =  RepositoryRuntimeRegistration.registerRepository(context, repository);
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
        }
    }
}
