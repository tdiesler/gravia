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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.repository.DefaultRepositoryResourceBuilder;
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.utils.NotNullException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract  {@link Repository} that does nothing.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public abstract class AbstractRepository implements Repository {

    public static final Logger LOGGER = LoggerFactory.getLogger(Repository.class.getPackage().getName());

    private final PropertiesProvider propertiesProvider;
    private RepositoryStorage storage;
    private Repository fallback;

    public AbstractRepository(PropertiesProvider propertyProvider) {
        this.propertiesProvider = propertyProvider;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public PropertiesProvider getPropertiesProvider() {
        return propertiesProvider;
    }

    public RepositoryStorage getRepositoryStorage() {
        return storage;
    }

    public void setRepositoryStorage(RepositoryStorage storage) {
        NotNullException.assertValue(storage, "storage");
        if (this.storage != null)
            throw new IllegalStateException("RepositoryStorage already set");
        this.storage = storage;
    }

    public Repository getFallbackRepository() {
        return fallback;
    }

    public void setFallbackRepository(Repository fallback) {
        NotNullException.assertValue(fallback, "fallback");
        if (this.fallback != null)
            throw new IllegalStateException("Fallback repository already set");
        this.fallback = fallback;
    }

    public RepositoryReader getRepositoryReader() {
        return getRequiredRepositoryStorage().getRepositoryReader();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        T result = null;
        if (type.isAssignableFrom(getClass())) {
            result = (T) this;
        } else if (RepositoryStorage.class.isAssignableFrom(type)) {
            result = (T) getRepositoryStorage();
        } else if (RepositoryReader.class.isAssignableFrom(type)) {
            result = (T) getRepositoryReader();
        } else if (fallback != null) {
            result = fallback.adapt(type);
        }
        return result;
    }

    @Override
    public Resource addResource(Resource res) {
        return getRequiredRepositoryStorage().addResource(res);
    }

    @Override
    public Resource addResource(Resource res, MavenCoordinates mavenid) {
        Capability icap = res.getIdentityCapability();
        String attkey = IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE;
        String attval = (String) icap.getAttributes().get(attkey);
        if (attval != null && !attval.equals(mavenid.toExternalForm()))
            throw new IllegalArgumentException("Resource already contains a " + attkey + " attribute: " + attval);

        ResourceBuilder builder = new DefaultRepositoryResourceBuilder();
        for (Capability aux : res.getCapabilities(null)) {
            Capability cap = builder.addCapability(aux.getNamespace(), aux.getAttributes(), aux.getDirectives());
            if (IdentityNamespace.IDENTITY_NAMESPACE.equals(cap.getNamespace())) {
                cap.getAttributes().put(attkey, mavenid.toExternalForm());
            }
        }
        for (Requirement aux : res.getRequirements(null)) {
            builder.addRequirement(aux.getNamespace(), aux.getAttributes(), aux.getDirectives());
        }
        Resource rescopy = builder.getResource();
        return getRequiredRepositoryStorage().addResource(rescopy);
    }

    @Override
    public Resource addResource(Resource res, URL contentURL) {
        return addResourceFromURL(res, contentURL);
    }

    @Override
    public Resource addResource(Resource res, InputStream content) throws IOException {
        File tempFile = copyResourceContent(content, res.getIdentity());
        URL contentURL = tempFile.toURI().toURL();
        try {
            return addResourceFromURL(res, contentURL);
        } finally {
            tempFile.delete();
        }
    }

    private Resource addResourceFromURL(Resource res, URL contentURL) {
        if (!res.getCapabilities(ContentNamespace.CONTENT_NAMESPACE).isEmpty())
            throw new IllegalArgumentException("Resource already contains a content capability: " + res);

        ResourceBuilder builder = new DefaultRepositoryResourceBuilder();
        Capability ccap = builder.addCapability(ContentNamespace.CONTENT_NAMESPACE, null);
        ccap.getAttributes().put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, contentURL.toExternalForm());
        for (Capability aux : res.getCapabilities(null)) {
            builder.addCapability(aux.getNamespace(), aux.getAttributes(), aux.getDirectives());
        }
        for (Requirement aux : res.getRequirements(null)) {
            builder.addRequirement(aux.getNamespace(), aux.getAttributes(), aux.getDirectives());
        }
        Resource rescopy = builder.getResource();
        return getRequiredRepositoryStorage().addResource(rescopy);
    }

    @Override
    public Resource removeResource(ResourceIdentity identity) {
        return getRequiredRepositoryStorage().removeResource(identity);
    }

    @Override
    public Resource getResource(ResourceIdentity identity) {
        return getRequiredRepositoryStorage().getResource(identity);
    }

    @Override
    public Map<Requirement, Collection<Capability>> findProviders(Collection<Requirement> reqs) {
        NotNullException.assertValue(reqs, "reqs");

        Map<Requirement, Collection<Capability>> result = new HashMap<Requirement, Collection<Capability>>();
        for (Requirement req : reqs) {
            Collection<Capability> providers = findProviders(req);
            result.put(req, providers);
        }

        return result;
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        NotNullException.assertValue(req, "req");

        // Try to find the providers in the storage
        RepositoryStorage repositoryStorage = getRequiredRepositoryStorage();
        Collection<Capability> providers = repositoryStorage.findProviders(req);

        // Try to find the providers in the fallback
        if (providers.isEmpty() && getFallbackRepository() != null) {
            providers = new HashSet<Capability>();
            for (Capability cap : getFallbackRepository().findProviders(req)) {
                Resource res = cap.getResource();
                ResourceIdentity resid = res.getIdentity();
                Resource storageResource = repositoryStorage.getResource(resid);
                if (storageResource == null) {
                    storageResource = repositoryStorage.addResource(res);
                    for (Capability aux : storageResource.getCapabilities(req.getNamespace())) {
                        if (cap.getAttributes().equals(aux.getAttributes())) {
                            cap = aux;
                            break;
                        }
                    }
                }
                providers.add(cap);
            }
        }
        return Collections.unmodifiableCollection(providers);
    }

    private RepositoryStorage getRequiredRepositoryStorage() {
        if (storage == null)
            throw new IllegalStateException("RepositoryStorage not set");
        return storage;
    }

    private File copyResourceContent(InputStream input, ResourceIdentity identity) throws IOException {
        String name = identity.getSymbolicName() + "-content";
        File targetFile = File.createTempFile(name, ".jar");
        int len = 0;
        byte[] buf = new byte[4096];
        OutputStream out = new FileOutputStream(targetFile);
        while ((len = input.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        input.close();
        out.close();
        return targetFile;
    }
}
