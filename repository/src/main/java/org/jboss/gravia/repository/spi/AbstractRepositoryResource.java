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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.gravia.repository.ContentCapability;
import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResource;

/**
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractRepositoryResource extends DefaultResource {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        T result = super.adapt(type);
        if (result == null) {
            if (type == RepositoryContent.class) {
                result = (T) getRepositoryContent();
            }
        }
        return result;
    }

    private RepositoryContent getRepositoryContent() {
        for (Capability cap : getCapabilities(ContentNamespace.CONTENT_NAMESPACE)) {
            ContentCapability ccap = cap.adapt(ContentCapability.class);
            final String contentURL = ccap.getContentURL();
            return new RepositoryContent() {
                @Override
                public InputStream getContent() {
                    try {
                        return new URL(contentURL).openStream();
                    } catch (IOException ex) {
                        throw new IllegalStateException("Cannot access content URL: " + contentURL, ex);
                    }
                }
            };
        }
        return null;
    }

}
