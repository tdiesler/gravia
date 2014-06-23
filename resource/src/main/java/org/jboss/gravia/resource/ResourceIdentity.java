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

import org.jboss.gravia.utils.IllegalArgumentAssertion;


/**
 * A resource identity.
 *
 * A resource is identified by its symbolic name and {@link Version}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2013
 */
public final class ResourceIdentity {

    private final String symbolicName;
    private final Version version;
    private final String canonicalForm;

    public static ResourceIdentity create(String symbolicName, String version) {
        return new ResourceIdentity(symbolicName, version != null ? Version.parseVersion(version) : null);
    }

    public static ResourceIdentity create(String symbolicName, Version version) {
        return new ResourceIdentity(symbolicName, version);
    }

    public static ResourceIdentity fromString(String identity) {
        int index = identity.indexOf(':');
        String namePart = index > 0 ? identity.substring(0, index) : identity;
        String versionPart = index > 0 ? identity.substring(index + 1) : "0.0.0";
        return new ResourceIdentity(namePart, Version.parseVersion(versionPart));
    }

    private ResourceIdentity(String symbolicName, Version version) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        this.symbolicName = symbolicName.trim();
        this.version = version != null ? version : Version.emptyVersion;
        this.canonicalForm = symbolicName + ":" + version;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public Version getVersion() {
        return version;
    }

    public String getCanonicalForm() {
        return canonicalForm;
    }

    @Override
    public int hashCode() {
        return canonicalForm.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ResourceIdentity)) return false;
        ResourceIdentity other = (ResourceIdentity) obj;
        return canonicalForm.equals(other.canonicalForm);
    }

    @Override
    public String toString() {
        return canonicalForm;
    }
}
