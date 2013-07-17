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

    private ResourceIdentity(String symbolicName, Version version) {
        if (symbolicName == null)
            throw new IllegalArgumentException("Null symbolicName");
        if (version == null)
            throw new IllegalArgumentException("Null version");
        this.symbolicName = symbolicName;
        this.version = version;
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
