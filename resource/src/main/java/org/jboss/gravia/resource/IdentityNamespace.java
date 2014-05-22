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
 * Identity namespace constants.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface IdentityNamespace extends Namespace {

    /**
     * Namespace name for identity capabilities and requirements.
     *
     * <p>
     * Also, the capability attribute used to specify the symbolic name of the
     * resource.
     */
    String IDENTITY_NAMESPACE = "gravia.identity";

    /**
     * The capability attribute identifying the {@code Version} of the resource
     * if one is specified or {@code 0.0.0} if not specified. The value of this
     * attribute must be of type {@code Version}.
     */
    String CAPABILITY_VERSION_ATTRIBUTE = "version";

    /**
     * The capability attribute defining this resource is to shared.
     * The default is 'false'.
     */
    String CAPABILITY_SHARED_ATTRIBUTE = "shared";

    /**
     * The capability attribute identifying the resource type. If the resource
     * has no type then the value {@link #TYPE_UNKNOWN unknown} must be used for
     * the attribute.
     *
     * @see #TYPE_ABSTRACT
     * @see #TYPE_BUNDLE
     * @see #TYPE_MODULE
     * @see #TYPE_UNKNOWN
     */
    String CAPABILITY_TYPE_ATTRIBUTE = "type";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as an OSGi bundle.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_BUNDLE = "osgi.bundle";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as a WildFly module.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_MODULE = "jboss.module";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as an abstract resource.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_ABSTRACT = "abstract";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as a reference resource.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_REFERENCE = "reference";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as unknown.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_UNKNOWN = "unknown";
}
