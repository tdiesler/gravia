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
package org.jboss.gravia.repository.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.gravia.repository.ContentCapability;
import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.repository.Namespace100.Attribute;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.Repository.ConfigurationPropertyProvider;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.repository.RepositoryStorageException;
import org.jboss.gravia.repository.RepositoryWriter;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;


/**
 * A simple {@link RepositoryStorage} that uses
 * the local file system.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractPersistentRepositoryStorage extends MemoryRepositoryStorage {

    private final AtomicLong increment = new AtomicLong();
    private ResourceBuilder resourceBuilder;

    public AbstractPersistentRepositoryStorage(Repository repository, ConfigurationPropertyProvider propertyProvider) {
        super(repository);
    }

    public void initRepositoryStorage() throws RepositoryStorageException {

        RepositoryReader reader = getPersistentRepositoryReader();
        if (reader != null) {
            String incatt = reader.getRepositoryAttributes().get(Attribute.INCREMENT.getLocalName());
            increment.set(incatt != null ? new Long(incatt) : increment.get());
            Resource res = reader.nextResource();
            while(res != null) {
                addResourceInternal(res, false);
                res = reader.nextResource();
            }
            reader.close();
        }
    }

    protected abstract ResourceBuilder createResourceBuilder();

    protected abstract RepositoryReader getPersistentRepositoryReader() throws RepositoryStorageException;

    protected abstract RepositoryWriter getPersistentRepositoryWriter() throws RepositoryStorageException;

    protected abstract void addResourceContent(InputStream input, Map<String, Object> atts) throws RepositoryStorageException;

    protected abstract URL getBaseURL();

    private ResourceBuilder getResourceBuilder() {
        if (resourceBuilder == null) {
            resourceBuilder = createResourceBuilder();
        }
        return resourceBuilder;
    }

    @Override
    public Resource addResource(Resource res) throws RepositoryStorageException {
        return addResourceInternal(res, true);
    }

    private synchronized Resource addResourceInternal(Resource res, boolean writeXML) throws RepositoryStorageException {
        if (res == null)
            throw new IllegalArgumentException("Null res");

        List<Capability> ccaps = res.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        if (ccaps.size() > 0) {
            return addContentResource(res, ccaps, writeXML);
        } else {
            return addAbstractResource(res, writeXML);
        }
    }

    private Resource addContentResource(Resource res, List<Capability> ccaps, boolean writeXML) throws RepositoryStorageException {

        String urlspec = (String) ccaps.get(0).getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
        if (urlspec == null)
            throw new IllegalArgumentException("Cannot obtain content URL from: " + res);

        Resource result;

        // Copy the resource to this storage, if the content URL does not match
        if (urlspec.startsWith(getBaseURL().toExternalForm()) == false) {
            ResourceBuilder builder = getResourceBuilder();
            for (Capability cap : res.getCapabilities(null)) {
                if (!ContentNamespace.CONTENT_NAMESPACE.equals(cap.getNamespace())) {
                    builder.addCapability(cap.getNamespace(), cap.getAttributes(), cap.getDirectives());
                } else {
                    ContentCapability ccap = cap.adapt(ContentCapability.class);
                    Map<String, Object> contentAtts = new HashMap<String, Object>();
                    String mimeType = (String) ccap.getAttribute(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE);
                    if (mimeType != null) {
                        contentAtts.put(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE, mimeType);
                    }
                    InputStream input = getResourceContent(ccap);
                    try {
                        addResourceContent(input, contentAtts);
                        builder.addCapability(ContentNamespace.CONTENT_NAMESPACE, contentAtts, cap.getDirectives());
                    } catch (RepositoryStorageException ex) {
                        throw new RepositoryStorageException("Cannot add resource to storeage: " + mimeType, ex);
                    }
                }
            }
            for (Requirement req : res.getRequirements(null)) {
                String namespace = req.getNamespace();
                builder.addRequirement(namespace, req.getAttributes(), req.getDirectives());
            }
            result = builder.getResource();
        } else {
            result = res;
        }

        result = super.addResource(result);
        if (writeXML == true) {
            writeRepositoryXML();
        }

        return result;
    }

    private Resource addAbstractResource(Resource res, boolean writeXML) throws RepositoryStorageException {
        Resource result = super.addResource(res);
        if (writeXML == true) {
            writeRepositoryXML();
        }
        return result;
    }

    @Override
    public Resource removeResource(ResourceIdentity resid) {
        return removeResourceInternal(resid, true);
    }

    private synchronized Resource removeResourceInternal(ResourceIdentity resid, boolean writeXML) {
        Resource res = getResource(resid);
        List<Capability> ccaps = res.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        if (!ccaps.isEmpty()) {
            Capability ccap = ccaps.iterator().next();
            String fileURL = (String) ccap.getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
            File contentFile = new File(fileURL.substring("file:".length()));
            if (contentFile.exists()) {
                deleteRecursive(contentFile.getParentFile());
            }
        }
        super.removeResource(res.getIdentity());
        if (writeXML == true) {
            writeRepositoryXML();
        }
        return res;
    }

    private InputStream getResourceContent(ContentCapability ccap) {
        InputStream input;
        Resource resource = ccap.getResource();
        Capability defaultContent = resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE).get(0);
        if (defaultContent == ccap) {
            input = resource.adapt(RepositoryContent.class).getContent();
        } else {
            String contentURL = ccap.getContentURL();
            try {
                input = new URL(contentURL).openStream();
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot access content URL: " + contentURL, ex);
            }
        }
        return input;
    }

    private void writeRepositoryXML() {
        RepositoryWriter writer;
        try {
            writer = getPersistentRepositoryWriter();
        } catch (RepositoryStorageException ex) {
            throw new IllegalStateException("Cannot initialize repository writer", ex);
        }
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(Attribute.NAME.getLocalName(), getRepository().getName());
        attributes.put(Attribute.INCREMENT.getLocalName(), increment.toString());
        writer.writeRepositoryElement(attributes);
        RepositoryReader reader = getRepositoryReader();
        Resource resource = reader.nextResource();
        while(resource != null) {
            writer.writeResource(resource);
            resource = reader.nextResource();
        }
        writer.close();
    }

    private boolean deleteRecursive(File file) {
        boolean result = true;
        if (file.isDirectory()) {
            for (File aux : file.listFiles())
                result &= deleteRecursive(aux);
        }
        result &= file.delete();
        return result;
    }
}
