/*
 * #%L
 * Gravia :: Resource
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
package org.jboss.gravia.resource;

/**
 * General namespace constants.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Namespace {

    /**
     * The capability directive used to specify the effective time for the
     * capability. The default value is {@link #EFFECTIVE_RESOLVE resolve}.
     *
     * @see #EFFECTIVE_RESOLVE resolve
     */
    String  CAPABILITY_EFFECTIVE_DIRECTIVE      = "effective";

    /**
     * The requirement directive used to specify the resolution type for a
     * requirement. The default value is {@link #RESOLUTION_MANDATORY mandatory}
     * .
     *
     * @see #RESOLUTION_MANDATORY mandatory
     * @see #RESOLUTION_OPTIONAL optional
     */
    String  REQUIREMENT_RESOLUTION_DIRECTIVE    = "resolution";

    /**
     * The directive value identifying a mandatory requirement resolution type.
     * A mandatory resolution type indicates that the requirement must be
     * resolved when the resource is resolved. If such a requirement cannot be
     * resolved, the resource fails to resolve.
     *
     * @see #REQUIREMENT_RESOLUTION_DIRECTIVE
     */
    String  RESOLUTION_MANDATORY                = "mandatory";

    /**
     * The directive value identifying an optional requirement resolution type.
     * An optional resolution type indicates that the requirement is optional
     * and the resource may be resolved without the requirement being resolved.
     *
     * @see #REQUIREMENT_RESOLUTION_DIRECTIVE
     */
    String  RESOLUTION_OPTIONAL                 = "optional";

    /**
     * The requirement directive used to specify the effective time for the
     * requirement. The default value is {@link #EFFECTIVE_RESOLVE resolve}.
     *
     * @see #EFFECTIVE_RESOLVE resolve
     */
    String  REQUIREMENT_EFFECTIVE_DIRECTIVE     = "effective";

    /**
     * The directive value identifying a {@link #CAPABILITY_EFFECTIVE_DIRECTIVE
     * capability} or {@link #REQUIREMENT_EFFECTIVE_DIRECTIVE requirement} that
     * is effective at resolve time. Capabilities and requirements with an
     * effective time of resolve are the only capabilities which are processed
     * while resolving a resource.
     *
     * @see #REQUIREMENT_EFFECTIVE_DIRECTIVE
     * @see #CAPABILITY_EFFECTIVE_DIRECTIVE
     */
    String  EFFECTIVE_RESOLVE                   = "resolve";
}
