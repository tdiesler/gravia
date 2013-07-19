package org.jboss.gravia.resource.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.jboss.logging.Logger;

/**
 * An abstract {@link ResourceStore}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResourceStore implements ResourceStore {
    
    static final Logger LOGGER = Logger.getLogger(Resource.class.getPackage().getName());
    
    private final String storeName;
    private final boolean logCapsReqs;
    private final Map<ResourceIdentity, Resource> resources = new LinkedHashMap<ResourceIdentity, Resource>();
    private final Map<CacheKey, Set<Capability>> capabilityCache = new ConcurrentHashMap<CacheKey, Set<Capability>>();
    private MatchPolicy matchPolicy;
    
    public AbstractResourceStore(String storeName) {
        this(storeName, false);
    }

    public AbstractResourceStore(String storeName, boolean logCapsReqs) {
        this.storeName = storeName;
        this.logCapsReqs = logCapsReqs;
    }

    protected MatchPolicy createMatchPolicy() {
        return new DefaultMatchPolicy();
    }

    @Override
    public Iterator<Resource> getResources() {
        synchronized (resources) {
            List<Resource> snapshot = new ArrayList<Resource>(resources.values());
            return snapshot.iterator();
        }
    }

    @Override
    public Resource addResource(Resource resource) {
        synchronized (resources) {
            LOGGER.debugf("Add to %s: %s", storeName, resource);
            
            // Add resource capabilites
            for (Capability cap : resource.getCapabilities(null)) {
                CacheKey cachekey = CacheKey.create(cap);
                getCachedCapabilities(cachekey).add(cap);
            }

            // Log cap/req details
            if (logCapsReqs) {
                for (Capability cap : resource.getCapabilities(null)) {
                    LOGGER.debugf("   %s", cap);
                }
                for (Requirement req : resource.getRequirements(null)) {
                    LOGGER.debugf("   %s", req);
                }
            }
            
            return resources.put(resource.getIdentity(), resource);
        }
    }

    @Override
    public Resource removeResource(ResourceIdentity identity) {
        synchronized (resources) {
            Resource res = resources.remove(identity);
            if (res != null) {
                
                LOGGER.debugf("Remove from %s: %s", storeName, res);

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
    public Resource getResource(ResourceIdentity identity) {
        synchronized (resources) {
            return resources.get(identity);
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

    private MatchPolicy getMatchPolicyInternal() {
        if (matchPolicy == null) {
            matchPolicy = createMatchPolicy();
        }
        return matchPolicy;
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
