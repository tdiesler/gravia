package org.jboss.gravia.resource;

/**
 * General namespace constants.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Namespace {

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

}
