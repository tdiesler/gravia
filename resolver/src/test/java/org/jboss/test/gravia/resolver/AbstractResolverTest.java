/*
 * #%L
 * Gravia :: Resolver
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
package org.jboss.test.gravia.resolver;

import static org.jboss.gravia.resolver.spi.ResolverLogger.LOGGER;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.resolver.DefaultEnvironment;
import org.jboss.gravia.resolver.DefaultResolveContext;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.Wiring;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;


/**
 * The abstract resolver test.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolverTest {

    @Rule public TestName testName = new TestName();

    Resolver resolver;
    Environment environment;

    @Before
    public void setUp() throws Exception {
        LOGGER.debug("Start: {}.{}", getClass().getSimpleName(), testName.getMethodName());
        resolver = new DefaultResolver();
        environment = new DefaultEnvironment("testStore");
    }

    @After
    public void tearDown() throws Exception {
        LOGGER.debug("End: {}.{}", getClass().getSimpleName(), testName.getMethodName());
    }

    ResourceStore installResources(Resource... resources) {
        for (Resource res : resources) {
            environment.addResource(res);
        }
        return environment;
    }

    ResolveContext getResolveContext(List<Resource> mandatory, List<Resource> optional) {
        Set<Resource> manres = mandatory != null ? new HashSet<Resource>(mandatory) : null;
        Set<Resource> optres = optional != null ? new HashSet<Resource>(optional) : null;
        return new DefaultResolveContext(environment, manres, optres);
    }

    Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        return resolver.resolve(context);
    }

    Map<Resource, List<Wire>> resolveAndApply(ResolveContext context) throws ResolutionException {
        return resolver.resolveAndApply(context);
    }

    Wiring getWiring(Resource resource) {
        return environment.getWirings().get(resource);
    }
}
