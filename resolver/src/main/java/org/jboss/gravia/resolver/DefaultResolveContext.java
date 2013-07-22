/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

package org.jboss.gravia.resolver;

import java.util.Set;

import org.jboss.gravia.resolver.spi.AbstractResolveContext;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;

/**
 * The default {@link ResolveContext}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public class DefaultResolveContext extends AbstractResolveContext {
    
    public DefaultResolveContext(ResourceStore resourceStore, Set<Resource> mandatory, Set<Resource> optional) {
        super(resourceStore, mandatory, optional);
    }

    @Override
    protected PreferencePolicy createPreferencePolicy() {
        return new DefaultPreferencePolicy();
    }
}
