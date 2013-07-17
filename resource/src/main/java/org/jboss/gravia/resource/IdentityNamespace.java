package org.jboss.gravia.resource;

/**
 * Identity namespace constants.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface IdentityNamespace {

    /**
     * Namespace name for identity capabilities and requirements.
     *
     * <p>
     * Also, the capability attribute used to specify the symbolic name of the
     * resource.
     */
    public static final String  IDENTITY_NAMESPACE                  = "gravia.identity";

    /**
     * The capability attribute identifying the {@code Version} of the resource
     * if one is specified or {@code 0.0.0} if not specified. The value of this
     * attribute must be of type {@code Version}.
     */
    public static final String  CAPABILITY_VERSION_ATTRIBUTE        = "version";

    /**
     * The capability attribute identifying the resource type. If the resource
     * has no type then the value {@link #TYPE_UNKNOWN unknown} must be used for
     * the attribute.
     *
     * @see #TYPE_BUNDLE
     * @see #TYPE_MODULE
     * @see #TYPE_UNKNOWN
     */
    public static final String  CAPABILITY_TYPE_ATTRIBUTE           = "type";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as an OSGi bundle.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    public static final String  TYPE_BUNDLE                         = "type.bundle";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as a WildFly module.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    public static final String  TYPE_MODULE                       = "type.module";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as unknown.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    public static final String  TYPE_UNKNOWN                        = "type.unknown";
}
