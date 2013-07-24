
package org.jboss.gravia.repository.spi;
/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.jboss.gravia.repository.ContentCapability;
import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.repository.RepositoryStorageException;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.spi.AbstractCapability;
import org.jboss.gravia.resource.spi.AbstractResource;

/**
 * Create an URL based resource
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractRepositoryResourceBuilder extends DefaultResourceBuilder {

    protected abstract AbstractCapability createContentCapability(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs);

    private void oldAbstractRepositoryResourceBuilder(URL contentURL, Map<String, Object> contentAtts) {
        Capability ccap = addCapability(ContentNamespace.CONTENT_NAMESPACE, contentAtts, null);
        Map<String, Object> atts = ccap.getAttributes();
        atts.put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, contentURL.toExternalForm());
        if (atts.get(ContentNamespace.CONTENT_NAMESPACE) == null)
            atts.put(ContentNamespace.CONTENT_NAMESPACE, ContentCapability.DEFAULT_DIGEST);
        if (atts.get(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE) == null)
            atts.put(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE, ContentCapability.DEFAULT_MIME_TYPE);
        if (atts.get(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE) == null)
            atts.put(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE, ContentCapability.DEFAULT_SIZE);
    }

    @Override
    protected AbstractCapability createCapability(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        if (ContentNamespace.CONTENT_NAMESPACE.equals(namespace))
            return createContentCapability(resource, namespace, atts, dirs);
        else
            return super.createCapability(resource, namespace, atts, dirs);
    }

    static class URLResource extends AbstractResource implements RepositoryContent {

        private final URL contentURL;

        URLResource(URL contentURL) {
            if (contentURL == null)
                throw new IllegalArgumentException("Null contentURL");
            this.contentURL = contentURL;
        }

        URL getContentURL() {
            return contentURL;
        }

        @Override
        public InputStream getContent() {
            try {
                if (contentURL.getProtocol().equals("file")) {
                    return new FileInputStream(new File(contentURL.getPath()));
                } else {
                    return contentURL.openStream();
                }
            } catch (IOException ex) {
                throw new RepositoryStorageException("Cannot obtain input stream for: " + contentURL, ex);
            }
        }
    }
}
