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
package org.jboss.gravia.resolver.spi;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.gravia.resolver.DefaultEnvironment;
import org.jboss.gravia.resolver.DefaultResolveContext;
import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.DefaultWiring;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.resource.spi.AbstractResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;


/**
 * The abstract resolver test.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolverInternalsTest {

    final Logger log = AbstractResolver.LOGGER;

    @Rule public TestName testName = new TestName();

    AbstractResolver resolver;
    AbstractEnvironment environment;

    @Before
    public void setUp() throws Exception {
        log.debug("Start: {}.{}", getClass().getSimpleName(), testName.getMethodName());

        environment = new DefaultEnvironment("testStore");
        resolver = new DefaultResolver();

        // A-1.0.0 => D-1.0.0, B-1.1.0
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        builder.addIdentityRequirement("resD", new VersionRange("[1.0,2.0)"));
        environment.addResource(builder.getResource());

        // D-1.0.0 => C-1.0.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resD", new Version("1.0.0"));
        builder.addIdentityRequirement("resC", new VersionRange("[1.0,2.0)"));
        environment.addResource(builder.getResource());

        // B-1.1.0 => C-1.0.0, E-1.1.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.1.0"));
        builder.addIdentityRequirement("resC", new VersionRange("[1.0,2.0)"));
        builder.addIdentityRequirement("resE", new VersionRange("[1.0,2.0)"));
        environment.addResource(builder.getResource());

        // B-1.0.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.0.0"));
        builder.addIdentityRequirement("resC", new VersionRange("[1.0,2.0)"));
        builder.addIdentityRequirement("resE", new VersionRange("[1.0,2.0)"));
        environment.addResource(builder.getResource());

        // C-1.1.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resC", new Version("1.1.0"));
        environment.addResource(builder.getResource());

        // C-1.0.0 (resolved)
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resC", new Version("1.0.0"));
        AbstractResource resC10 = (AbstractResource) builder.getResource();
        environment.putWiring(resC10, new DefaultWiring(resC10, null, null));
        environment.addResource(resC10);

        // E-1.1.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resE", new Version("1.1.0"));
        environment.addResource(builder.getResource());

        // E-1.0.0
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resE", new Version("1.0.0"));
        environment.addResource(builder.getResource());
    }

    @After
    public void tearDown() throws Exception {
        log.debug("End: {}.{}", getClass().getSimpleName(), testName.getMethodName());
    }

    ResolveContext getResolveContext(List<Resource> mandatory, List<Resource> optional) {
        Set<Resource> manres = mandatory != null ? new LinkedHashSet<Resource>(mandatory) : null;
        Set<Resource> optres = optional != null ? new LinkedHashSet<Resource>(optional) : null;
        return new DefaultResolveContext(environment, manres, optres);
    }
}
