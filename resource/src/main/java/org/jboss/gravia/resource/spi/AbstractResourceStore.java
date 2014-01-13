/*
 * #%L
 * Gravia Resource
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.jboss.gravia.resource.spi;

import static org.jboss.gravia.resource.spi.AbstractResource.LOGGER;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultMatchPolicy;
import org.jboss.gravia.resource.MatchPolicy;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.utils.NotNullException;

/**
 * An abstract {@link ResourceStore}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractResourceStore implements ResourceStore {

    private final String storeName;
    private final boolean logCapsReqs;
    private final Map<ResourceIdentity, Resource> resources = new LinkedHashMap<ResourceIdentity, Resource>();
    private final Map<CacheKey, Set<Capability>> capabilityCache = new ConcurrentHashMap<CacheKey, Set<Capability>>();
    private final MatchPolicy matchPolicy;

    public AbstractResourceStore(String storeName) {
        this(storeName, new DefaultMatchPolicy(), false);
    }

    public AbstractResourceStore(String storeName, MatchPolicy matchPolicy, boolean logCapsReqs) {
        NotNullException.assertValue(storeName, "storeName");
        NotNullException.assertValue(matchPolicy, "matchPolicy");
        this.storeName = storeName;
        this.matchPolicy = matchPolicy;
        this.logCapsReqs = logCapsReqs;
    }


    @Override
    public String getName() {
        return storeName;
    }

    private MatchPolicy getMatchPolicyInternal() {
        return matchPolicy;
    }

    @Override
    public Iterator<Resource> getResources() {
        final Iterator<Resource> itres;
        synchronized (resources) {
            Set<Resource> snapshot = new LinkedHashSet<Resource>(resources.values());
            itres = snapshot.iterator();
        }
        return new Iterator<Resource>() {

            @Override
            public boolean hasNext() {
                synchronized (resources) {
                    return itres.hasNext();
                }
            }

            @Override
            public Resource next() {
                synchronized (resources) {
                    return itres.next();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Resource addResource(Resource res) {
        synchronized (resources) {

            if (getResource(res.getIdentity()) != null)
                throw new IllegalArgumentException("Resource already added: " + res);

            LOGGER.debug("Add to {}: {}", storeName, res);

            // Add resource capabilites
            for (Capability cap : res.getCapabilities(null)) {
                CacheKey cachekey = CacheKey.create(cap);
                getCachedCapabilities(cachekey).add(cap);
            }

            // Log cap/req details
            if (logCapsReqs) {
                for (Capability cap : res.getCapabilities(null)) {
                    LOGGER.debug("   {}", cap);
                }
                for (Requirement req : res.getRequirements(null)) {
                    LOGGER.debug("   {}", req);
                }
            }

            resources.put(res.getIdentity(), res);
            return res;
        }
    }

    @Override
    public Resource removeResource(ResourceIdentity resid) {
        synchronized (resources) {
            Resource res = resources.remove(resid);
            if (res != null) {

                LOGGER.debug("Remove from {}: {}", storeName, res);

                // Remove resource capabilities
                for (Capability cap : res.getCapabilities(null)) {
                    CacheKey cachekey = CacheKey.create(cap);
                    Set<Capability> cachecaps = getCachedCapabilities(cachekey);
                    cachecaps.remove(cap);
                    if (cachecaps.isEmpty()) {
                        capabilityCache.remove(cachekey);
                    }
                }
            }
            return res;
        }
    }

    @Override
    public Resource getResource(ResourceIdentity resid) {
        synchronized (resources) {
            return resources.get(resid);
        }
    }

    @Override
    public Set<Capability> findProviders(Requirement req) {
        CacheKey cachekey = CacheKey.create(req);
        Set<Capability> result = new HashSet<Capability>();
        for (Capability cap : findCachedCapabilities(cachekey)) {
            if (getMatchPolicyInternal().match(cap, req)) {
                result.add(cap);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private synchronized Set<Capability> getCachedCapabilities(CacheKey key) {
        Set<Capability> capset = capabilityCache.get(key);
        if (capset == null) {
            capset = new LinkedHashSet<Capability>();
            capabilityCache.put(key, capset);
        }
        return capset;
    }

    private synchronized Set<Capability> findCachedCapabilities(CacheKey key) {
        Set<Capability> capset = capabilityCache.get(key);
        if (capset == null) {
            capset = new LinkedHashSet<Capability>();
            // do not add this to the capabilityCache
        }
        if (capset.isEmpty() && (key.value == null)) {
            for (Entry<CacheKey, Set<Capability>> entry : capabilityCache.entrySet()) {
                CacheKey auxkey = entry.getKey();
                if (auxkey.namespace.equals(key.namespace)) {
                    capset.addAll(entry.getValue());
                }
            }
        }
        return Collections.unmodifiableSet(capset);
    }

    @Override
    public String toString() {
        String prefix = getClass() != AbstractResourceStore.class ? getClass().getSimpleName() : ResourceStore.class.getSimpleName();
        return prefix + "[" + storeName + "]";
    }

    private static class CacheKey {

        private final String namespace;
        private final String value;
        private final String keyspec;

        static CacheKey create(Capability cap) {
            String namespace = cap.getNamespace();
            String nsvalue = (String) cap.getAttributes().get(namespace);
            return new CacheKey(namespace, nsvalue);
        }

        static CacheKey create(Requirement req) {
            String namespace = req.getNamespace();
            String nsvalue = (String) req.getAttributes().get(namespace);
            return new CacheKey(namespace, nsvalue);
        }

        private CacheKey(String namespace, String value) {
            this.namespace = namespace;
            this.value = value;
            this.keyspec = namespace + ":" + value;
        }

        @Override
        public int hashCode() {
            return keyspec.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof CacheKey))
                return false;
            CacheKey other = (CacheKey) obj;
            return keyspec.equals(other.keyspec);
        }

        @Override
        public String toString() {
            return "[" + keyspec + "]";
        }
    }
}
