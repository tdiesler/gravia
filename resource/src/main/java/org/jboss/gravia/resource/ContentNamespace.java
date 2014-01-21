/*
 * #%L
 * Gravia :: Resource
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
