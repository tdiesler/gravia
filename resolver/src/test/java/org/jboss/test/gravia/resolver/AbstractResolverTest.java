/*
 * #%L
 * JBossOSGi Resolver Felix
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
package org.jboss.test.gravia.resolver;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.resolver.DefaultResolveContext;
import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resolver.spi.DefaultResolver;
import org.jboss.gravia.resource.DefaultResourceStore;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Wire;
import org.junit.Before;


/**
 * The abstract resolver test.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolverTest {

    Resolver resolver;
    ResourceStore resourceStore;

    @Before
    public void setUp() throws Exception {
        resolver = new DefaultResolver();
        resourceStore = new DefaultResourceStore("testStore");
    }

    ResourceStore installResources(Resource... resources) {
        for (Resource res : resources) {
            resourceStore.addResource(res);
        }
        return resourceStore;
    }

    ResolveContext getResolveContext(List<Resource> mandatory, List<Resource> optional) {
        Set<Resource> manres = mandatory != null ? new HashSet<Resource>(mandatory) : null;
        Set<Resource> optres = optional != null ? new HashSet<Resource>(optional) : null;
        return new DefaultResolveContext(resourceStore, manres, optres);
    }

    Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        return resolver.resolve(context);
    }
}
