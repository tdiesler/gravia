package org.jboss.gravia.repository.spi;

/*
 * #%L
 * JBossOSGi Repository
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
public abstract class AbstractRepositoryResource extends DefaultResource implements RepositoryContent {

    private static final long serialVersionUID = 3826729560161290645L;

    @Override
    public InputStream getContent() {
        for (Capability cap : getCapabilities(ContentNamespace.CONTENT_NAMESPACE)) {
            ContentCapability ccap = cap.adapt(ContentCapability.class);
            String contentURL = ccap.getContentURL();
            try {
                return new URL(contentURL).openStream();
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot access content URL: " + contentURL, ex);
            }
        }
        return null;
    }
}
