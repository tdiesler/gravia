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

package org.jboss.gravia.resource.spi;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.ContentNamespace;

/**
 * The abstract implementation of a {@link ContentCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 05-Jul-2012
 */
public class AbstractContentCapability extends AbstractCapability implements ContentCapability {

    private String mimeType;
    private String digest;
    private InputStream contentStream;
    private URL contentURL;
    private Long size;

    public AbstractContentCapability(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        super(resource, namespace, replaceAttributeTypes(atts), dirs);
        if (getAttribute(ContentNamespace.CONTENT_NAMESPACE) == null)
            getAttributes().put(ContentNamespace.CONTENT_NAMESPACE, ContentCapability.DEFAULT_DIGEST);
        if (getAttribute(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE) == null)
            getAttributes().put(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE, ContentCapability.DEFAULT_MIME_TYPE);
        if (getAttribute(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE) == null)
            getAttributes().put(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE, ContentCapability.DEFAULT_SIZE);
    }

    private static Map<String, Object> replaceAttributeTypes(Map<String, Object> atts) {
        Map<String, Object> result = new HashMap<String, Object>(atts);
        Object val = result.get(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE);
        if (val instanceof String) {
            result.put(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE, Long.parseLong((String) val));
        }
        val = result.get(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
        if (val instanceof String) {
            result.put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, toURL((String) val));
        }
        return result;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getDigest() {
        return digest;
    }

    @Override
    public InputStream getContentStream() {
        return contentStream;
    }

    @Override
    public URL getContentURL() {
        return contentURL;
    }

    @Override
    public Long getSize() {
        return size;
    }

    @Override
    public void validate() {
        super.validate();
        if (ContentNamespace.CONTENT_NAMESPACE.equals(getNamespace())) {

            digest = (String) getAttribute(ContentNamespace.CONTENT_NAMESPACE);
            if (digest == null)
                throw illegalStateCannotObtainAttribute(ContentNamespace.CONTENT_NAMESPACE);

            mimeType = (String) getAttribute(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE);
            if (mimeType == null)
                throw illegalStateCannotObtainAttribute(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE);

            contentURL = (URL) getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
            contentStream = (InputStream) getAttribute(ContentNamespace.CAPABILITY_STREAM_ATTRIBUTE);
            if (contentURL == null && contentStream == null)
                throw illegalStateCannotObtainAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);

            size = (Long) getAttribute(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE);
            if (size == null)
                throw illegalStateCannotObtainAttribute(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE);
        }
    }

    private IllegalStateException illegalStateCannotObtainAttribute(String attrName) {
        return new IllegalStateException("Cannot obtain attribute: " + attrName);
    }

    private static URL toURL(String urlspec) {
        try {
            return new URL(urlspec);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
