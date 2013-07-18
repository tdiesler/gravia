package org.jboss.gravia.resource;


/**
 * A capability match policy
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface MatchPolicy {

    /**
     * True if the requirement matches the given capability
     */
    boolean match(Capability cap, Requirement req);
}
