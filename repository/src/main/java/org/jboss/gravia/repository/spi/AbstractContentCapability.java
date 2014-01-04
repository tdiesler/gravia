/*
 * #%L
 * JBossOSGi Resolver API
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

package org.jboss.gravia.repository.spi;

import java.util.HashMap;
import java.util.Map;

import org.jboss.gravia.repository.ContentCapability;
import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.resource.spi.AbstractCapability;
import org.jboss.gravia.resource.spi.AbstractResource;

/**
 * The abstract implementation of a {@link ContentCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 05-Jul-2012
 */
public class AbstractContentCapability extends AbstractCapability implements ContentCapability {

    private static final long serialVersionUID = -1477324375627957571L;

    private String mimeType;
    private String digest;
    private String contentURL;
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
            result.put(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE, Long.parseLong((String)val));
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
    public String getContentURL() {
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
            contentURL = (String) getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
            if (contentURL == null)
                throw illegalStateCannotObtainAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
            size = (Long) getAttribute(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE);
            if (size == null)
                throw illegalStateCannotObtainAttribute(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE);
        }
    }

    private IllegalStateException illegalStateCannotObtainAttribute(String attrName) {
        return new IllegalStateException("Cannot obtain attribute: " + attrName);
    }
}
