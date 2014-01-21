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
package org.jboss.gravia.resolver;

import java.util.List;

import org.jboss.gravia.resolver.spi.AbstractResolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultWire;
import org.jboss.gravia.resource.DefaultWiring;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.spi.AbstractWire;
import org.jboss.gravia.resource.spi.AbstractWiring;

/**
 * The default {@link Resolver}.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class DefaultResolver extends AbstractResolver {

    @Override
    protected AbstractWire createWire(Requirement req, Capability cap) {
        return new DefaultWire(req, cap);
    }

    @Override
    protected AbstractWiring createWiring(Resource resource, List<Wire> reqwires, List<Wire> provwires) {
        return new DefaultWiring(resource, reqwires, provwires);
    }
    
}
