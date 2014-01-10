/*
 * #%L
 * Gravia Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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
 * Content Capability Namespace.
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
     * The capability attribute that contains an InputStream to the content.
     */
    String  CAPABILITY_STREAM_ATTRIBUTE    = "stream";

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
