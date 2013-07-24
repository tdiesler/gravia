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
     * Artifact coordinates may be defined by the simple groupId:artifactId:version form,
     * or the fully qualified form groupId:artifactId:type:version[:classifier]
     */
    String CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE = "maven.identity";

    /**
     * The capability attribute identifying the {@code Version} of the resource
     * if one is specified or {@code 0.0.0} if not specified. The value of this
     * attribute must be of type {@code Version}.
     */
    String CAPABILITY_VERSION_ATTRIBUTE = "version";

    /**
     * The capability attribute identifying the resource type. If the resource
     * has no type then the value {@link #TYPE_UNKNOWN unknown} must be used for
     * the attribute.
     *
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
    String TYPE_BUNDLE = "type.bundle";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as a WildFly module.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_MODULE = "type.module";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as an abstract resource.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_ABSTRACT = "abstract";

    /**
     * The attribute value identifying the resource
     * {@link #CAPABILITY_TYPE_ATTRIBUTE type} as unknown.
     *
     * @see #CAPABILITY_TYPE_ATTRIBUTE
     */
    String TYPE_UNKNOWN = "type.unknown";
}
