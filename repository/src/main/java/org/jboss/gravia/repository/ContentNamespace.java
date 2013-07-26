package org.jboss.gravia.repository;

import org.jboss.gravia.resource.Namespace;


/**
 * Content Capability and Requirement Namespace.
 *
 * <p>
 * This class defines the names for the attributes and directives for this
 * namespace. All unspecified capability attributes are of type {@code String}
 * and are used as arbitrary matching attributes for the capability. The values
 * associated with the specified directive and attribute keys are of type
 * {@code String}, unless otherwise indicated.
 *
 * @author thomas.diesler@jboss.com
 * @since 05-Jul-2012
 */
public interface ContentNamespace extends Namespace {

	/**
	 * Namespace name for content capabilities and requirements.
	 *
	 * <p>
	 * Also, the capability attribute used to specify the unique identifier of
	 * the content. This identifier is the {@code SHA-256} hash of the content.
	 */
	String	CONTENT_NAMESPACE			= "gravia.content";

	/**
	 * The capability attribute that contains the URL to the content.
	 */
	String	CAPABILITY_URL_ATTRIBUTE	= "url";

	/**
	 * The capability attribute that contains the size, in bytes, of the
	 * content. The value of this attribute must be of type {@code Long}.
	 */
	String	CAPABILITY_SIZE_ATTRIBUTE	= "size";

	/**
	 * The capability attribute that defines the IANA MIME Type/Format for this
	 * content.
	 */
	String	CAPABILITY_MIME_ATTRIBUTE	= "mime";
}
