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
package org.jboss.gravia.utils;

import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Resource;


/**
 * A utility class for {@link Resource}
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-Sep-2010
 */
public final class ResourceUtils {

    // Hide ctor
    private ResourceUtils() {
    }

    /**
     * A resource is abstract if it has a 'type' attribute with value 'abstract' or 'reference'
     */
    public static boolean isAbstract(Resource res) {
        Object attval = res.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE);
        return isReference(res) || IdentityNamespace.TYPE_ABSTRACT.equals(attval);
    }

    /**
     * A reference resource has a 'type' attribute with value 'reference'
     */
    public static boolean isReference(Resource res) {
        Object attval = res.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE);
        return IdentityNamespace.TYPE_REFERENCE.equals(attval);
    }

    /**
     * A shared resource has a 'shared' attribute with value 'true'
     */
    public static boolean isShared(Resource resource) {
        Object attval = resource.getIdentityCapability().getAttribute(IdentityNamespace.CAPABILITY_SHARED_ATTRIBUTE);
        return Boolean.parseBoolean(attval != null ? attval.toString() : null);
    }
}
