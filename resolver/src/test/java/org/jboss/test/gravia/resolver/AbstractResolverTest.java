/*
 * #%L
 * JBossOSGi Resolver Felix
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.resolver;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.resolver.DefaultResolveContext;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.DefaultResourceStore;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Wire;
import org.jboss.logging.Logger;
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

    final Logger log = Logger.getLogger(getClass());
    
    @Rule public TestName testName = new TestName();
    
    Resolver resolver;
    ResourceStore resourceStore;

    @Before
    public void setUp() throws Exception {
        log.debugf("Start: %s.%s", getClass().getSimpleName(), testName.getMethodName());
        resolver = new DefaultResolver();
        resourceStore = new DefaultResourceStore("testStore", true);
    }
    
    @After
    public void tearDown() throws Exception {
        log.debugf("End: %s.%s", getClass().getSimpleName(), testName.getMethodName());
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

    Map<Resource, List<Wire>> resolveAndApply(ResolveContext context) throws ResolutionException {
        return resolver.resolveAndApply(context);
    }
}
