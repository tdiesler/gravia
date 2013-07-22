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
package org.jboss.gravia.resolver.spi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.gravia.resolver.DefaultResolveContext;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.DefaultResourceStore;
import org.jboss.gravia.resource.DefaultWiring;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.resource.spi.AbstractResource;
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
public abstract class AbstractResolverInternalsTest {

    final Logger log = Logger.getLogger(getClass());
    
    @Rule public TestName testName = new TestName();
    
    AbstractResolver resolver;
    ResourceStore store;

    @Before
    public void setUp() throws Exception {
        log.debugf("Start: %s.%s", getClass().getSimpleName(), testName.getMethodName());
        
        store = new DefaultResourceStore("testStore", true);
        resolver = new DefaultResolver();
         
        // A-1.0.0 => D-1.0.0, B-1.1.0
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        builder.addIdentityRequirement("resD", new VersionRange("[1.0,2.0)"));
        store.addResource(builder.getResource());
        
        // D-1.0.0 => C-1.0.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resD", new Version("1.0.0"));
        builder.addIdentityRequirement("resC", new VersionRange("[1.0,2.0)"));
        store.addResource(builder.getResource());
        
        // B-1.1.0 => C-1.0.0, E-1.1.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.1.0"));
        builder.addIdentityRequirement("resC", new VersionRange("[1.0,2.0)"));
        builder.addIdentityRequirement("resE", new VersionRange("[1.0,2.0)"));
        store.addResource(builder.getResource());
        
        // B-1.0.0 
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.0.0"));
        builder.addIdentityRequirement("resC", new VersionRange("[1.0,2.0)"));
        builder.addIdentityRequirement("resE", new VersionRange("[1.0,2.0)"));
        store.addResource(builder.getResource());
        
        // C-1.1.0 
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resC", new Version("1.1.0"));
        store.addResource(builder.getResource());
        
        // C-1.0.0 (resolved)
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resC", new Version("1.0.0"));
        AbstractResource resC10 = (AbstractResource) builder.getResource();
        resC10.setWiring(new DefaultWiring(resC10, null, null));
        store.addResource(resC10);
        
        // E-1.1.0 
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resE", new Version("1.1.0"));
        store.addResource(builder.getResource());
        
        // E-1.0.0 
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resE", new Version("1.0.0"));
        store.addResource(builder.getResource());
    }
    
    @After
    public void tearDown() throws Exception {
        log.debugf("End: %s.%s", getClass().getSimpleName(), testName.getMethodName());
    }

    ResolveContext getResolveContext(List<Resource> mandatory, List<Resource> optional) {
        Set<Resource> manres = mandatory != null ? new HashSet<Resource>(mandatory) : null;
        Set<Resource> optres = optional != null ? new HashSet<Resource>(optional) : null;
        return new DefaultResolveContext(store, manres, optres);
    }
}
