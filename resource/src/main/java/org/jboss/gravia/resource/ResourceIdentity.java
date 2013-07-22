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
