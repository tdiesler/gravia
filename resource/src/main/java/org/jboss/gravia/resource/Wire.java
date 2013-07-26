package org.jboss.gravia.resource;

/**
 * A wire connecting a {@link Capability} to a {@link Requirement}.
 *
 * <p>
 * Instances of this type must be <i>effectively immutable</i>. That is, for a
 * given instance of this interface, the methods defined by this interface must
 * always return the same result.
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Feb-2013
 */
public interface Wire {

    Capability getCapability();

    Requirement getRequirement();

    Resource getProvider();

    Resource getRequirer();
}
