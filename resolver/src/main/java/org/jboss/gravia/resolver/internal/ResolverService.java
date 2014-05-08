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
package org.jboss.gravia.resolver.internal;

import java.util.List;
import java.util.Map;

import org.jboss.gravia.resolver.DefaultResolver;
import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Wire;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link Resolver} component.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-May-2014
 */
@Component(service = { Resolver.class }, configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public final class ResolverService implements Resolver {

    private Resolver delegate = new DefaultResolver();

    @Override
    public Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        return delegate.resolve(context);
    }

    @Override
    public Map<Resource, List<Wire>> resolveAndApply(ResolveContext context) throws ResolutionException {
        return delegate.resolveAndApply(context);
    }

}
