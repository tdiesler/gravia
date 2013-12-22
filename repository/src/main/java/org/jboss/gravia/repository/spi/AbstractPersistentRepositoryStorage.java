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
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.MavenIdentityRepository;
import org.jboss.gravia.repository.Namespace100.Attribute;
import org.jboss.gravia.repository.PersistentRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryAggregator;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.repository.RepositoryStorageException;
import org.jboss.gravia.repository.RepositoryWriter;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * A simple {@link RepositoryStorage} that uses
 * the local file system.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractPersistentRepositoryStorage extends MemoryRepositoryStorage {

    private final AtomicLong increment = new AtomicLong();

    public AbstractPersistentRepositoryStorage(PersistentRepository repository, PropertiesProvider propertyProvider) {
        super(repository);
    }

    public void initRepositoryStorage() throws RepositoryStorageException {

        RepositoryReader reader = getPersistentRepositoryReader();
        if (reader != null) {
            String incatt = reader.getRepositoryAttributes().get(Attribute.INCREMENT.getLocalName());
            increment.set(incatt != null ? new Long(incatt) : increment.get());
            Resource res = reader.nextResource();
            while (res != null) {
                addResourceInternal(res, false);
                res = reader.nextResource();
            }
            reader.close();
        }
    }

    @Override
    public PersistentRepository getRepository() {
        return (PersistentRepository) super.getRepository();
    }

    protected abstract ResourceBuilder createResourceBuilder();

    protected abstract RepositoryReader getPersistentRepositoryReader() throws RepositoryStorageException;

    protected abstract RepositoryWriter getPersistentRepositoryWriter() throws RepositoryStorageException;

    protected abstract void addResourceContent(InputStream input, Map<String, Object> atts) throws RepositoryStorageException;

    protected abstract URL getBaseURL();

    @Override
    public Resource addResource(Resource res) throws RepositoryStorageException {
        return addResourceInternal(res, true);
    }

    private synchronized Resource addResourceInternal(Resource resource, boolean writeXML) throws RepositoryStorageException {
        if (resource == null)
            throw new IllegalArgumentException("Null resource");

        // Convert to a maven resource if needed
        Capability icap = resource.getIdentityCapability();
        List<Capability> ccaps = resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        String mvnatt = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE);
        if (ccaps.isEmpty() && mvnatt != null) {
            MavenCoordinates mvnid = MavenCoordinates.parse(mvnatt);
            Resource mvnres = getMavenResource(mvnid);
            ccaps = mvnres.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        }

        if (ccaps.size() > 0) {
            return addContentResource(resource, ccaps, writeXML);
        } else {
            return addAbstractResource(resource, writeXML);
        }
    }

    private Resource getMavenResource(MavenCoordinates mavenid) {
        MavenIdentityRepository mvnrepo = null;
        Repository delegate = getRepository().getDelegate();
        if (delegate instanceof MavenIdentityRepository) {
            mvnrepo = (MavenIdentityRepository) delegate;
        } else if (delegate instanceof RepositoryAggregator) {
            RepositoryAggregator aggregator = (RepositoryAggregator) delegate;
            for (Repository repo : aggregator.getDelegates()) {
                if (repo instanceof MavenIdentityRepository) {
                    mvnrepo = (MavenIdentityRepository) repo;
                    break;
                }
            }
        }
        return mvnrepo != null ? mvnrepo.findMavenResource(mavenid) : null;
    }

    private Resource addContentResource(Resource res, List<Capability> ccaps, boolean writeXML) throws RepositoryStorageException {

        String urlspec = (String) ccaps.get(0).getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
        if (urlspec == null)
            throw new IllegalArgumentException("Cannot obtain content URL from: " + res);

        Resource result;

        // Copy the resource to this storage, if the content URL does not match
        if (urlspec.startsWith(getBaseURL().toExternalForm()) == false) {
            ResourceBuilder builder = createResourceBuilder();
            for (Capability cap : res.getCapabilities(null)) {
                if (!ContentNamespace.CONTENT_NAMESPACE.equals(cap.getNamespace())) {
                    builder.addCapability(cap.getNamespace(), cap.getAttributes(), cap.getDirectives());
                }
            }
            for (Capability cap : ccaps) {
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
        while (resource != null) {
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
