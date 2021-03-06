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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.spi.Resolver;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Wiring;

/**
 * A resolve context provides resources, options and constraints to the
 * potential solution of a {@link Resolver#resolve(ResolveContext) resolve}
 * operation.
 *
 * <p>
 * Resolve Contexts:
 * <ul>
 * <li>Specify the mandatory and optional resources to resolve. The mandatory
 * and optional resources must be consistent and correct. For example, they must
 * not violate the singleton policy of the implementer.</li>
 * <li>Provide {@link Capability capabilities} that the Resolver can use to
 * satisfy {@link Requirement requirements} via the
 * {@link #findProviders(Requirement)} method</li>
 * <li>Constrain solutions via the {@link #getWirings()} method. A wiring
 * consists of a map of existing {@link Resource resources} to {@link Wiring
 * wiring}.</li>
 * <li>Filter requirements that are part of a resolve operation via the
 * {@link #isEffective(Requirement)}.</li>
 * </ul>
 *
 * <p>
 * A resolver may call the methods on the resolve context any number of times
 * during a resolve operation using any thread. Implementors should ensure that
 * this class is properly thread safe.
 *
 * <p>
 * The resolve context methods must be <i>idempotent</i>. This means that resources
 * must have constant capabilities and requirements and the resolve context must
 * return a consistent set of capabilities, wires and effective requirements.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public interface ResolveContext {

    /**
     * Return the resources that must be resolved for this resolve context.
     *
     * <p>
     * The default implementation returns an empty collection.
     *
     * @return The resources that must be resolved for this resolve context. May
     *         be empty if there are no mandatory resources.
     */
    Collection<Resource> getMandatoryResources();

    /**
     * Return the resources that the resolver should attempt to resolve for this
     * resolve context. Inability to resolve one of the specified resources will
     * not result in a resolution exception.
     *
     * <p>
     * The default implementation returns an empty collection.
     *
     * @return The resources that the resolver should attempt to resolve for
     *         this resolve context. May be empty if there are no mandatory
     *         resources.
     */
    Collection<Resource> getOptionalResources();

    /**
     * Find Capabilities that match the given Requirement.
     * <p>
     * The returned list contains {@link Capability} objects where the Resource
     * must be the declared Resource of the Capability.
     *
     * <p>
     * The returned list is in priority order such that the Capabilities with a
     * lower index have a preference over those with a higher index.
     *
     * <p>
     * Each returned Capability must match the given Requirement.
     *
     * @param requirement The requirement that a resolver is attempting to
     *        satisfy. Must not be {@code null}.
     * @return A list of {@link Capability} objects that match the specified
     *         requirement.
     */
    List<Capability> findProviders(Requirement requirement);

    /**
     * Test if a given requirement should be wired in the resolve operation. If
     * this method returns {@code false}, then the resolver should ignore this
     * requirement during the resolve operation.
     *
     * <p>
     * The primary use case for this is to test the {@code effective} directive
     * on the requirement, though implementations are free to use any effective
     * test.
     *
     * @param requirement The Requirement to test. Must not be {@code null}.
     * @return {@code true} if the requirement should be considered as part of
     *         the resolve operation.
     */
    boolean isEffective(Requirement requirement);

    /**
     * Returns the wirings for existing resolved resources.
     *
     * <p>
     * Multiple calls to this method for this resolve context must return the
     * same result.
     *
     * @return The wirings for existing resolved resources. The returned map is
     *         unmodifiable.
     */
    Map<Resource, Wiring> getWirings();

}
