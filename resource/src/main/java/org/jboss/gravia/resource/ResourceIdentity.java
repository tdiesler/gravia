/*
 * #%L
 * Gravia Resource
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.gravia.resource;

/**
 * An identity capability
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2013
 */
public final class ResourceIdentity {

    private final String symbolicName;
    private final Version version;

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
        if (symbolicName == null)
            throw new IllegalArgumentException("Null symbolicName");
        this.symbolicName = symbolicName;
        this.version = version != null ? version : Version.emptyVersion;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ResourceIdentity))
            return false;
        if (other == this)
            return true;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return symbolicName + ":" + version;
    }
}
