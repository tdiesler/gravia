/*
 * #%L
 * JBossOSGi Resolver Metadata
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
package org.jboss.gravia.runtime.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * CaseInsensitiveDictionary.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Thomas.Diesler@jboss.com
 */
@SuppressWarnings("rawtypes")
public class CaseInsensitiveDictionary extends Hashtable {
    private static final long serialVersionUID = 5802491129524016545L;

    /** The delegate dictionary */
    private Dictionary<String, Object> delegate;

    /** The original keys */
    private Set<String> originalKeys;

    /**
     * Create a new CaseInsensitiveDictionary.
     * 
     * @param delegate the delegate
     */
    @SuppressWarnings("unchecked")
    public CaseInsensitiveDictionary(Dictionary delegate) {
        if (delegate == null)
            throw new IllegalArgumentException("Null delegate");

        this.delegate = new Hashtable<String, Object>(delegate.size());
        this.originalKeys = Collections.synchronizedSet(new HashSet<String>());
        Enumeration<String> e = delegate.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            if (get(key) != null)
                throw new IllegalArgumentException("Duplicate key '" + key + "' in: " + delegate);

            this.delegate.put(key.toLowerCase(Locale.ENGLISH), delegate.get(key));
            originalKeys.add(key);
        }
    }

    @Override
    public Enumeration<Object> elements() {
        return delegate.elements();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj instanceof Dictionary == false)
            return false;

        Dictionary<String, Object> other = (Dictionary) obj;

        if (size() != other.size())
            return false;
        if (isEmpty())
            return true;

        for (String key : originalKeys) {
            if (get(key).equals(other.get(key)))
                return false;
        }
        return true;
    }

    @Override
    public Object get(Object key) {
        if (key instanceof String)
            key = ((String) key).toLowerCase(Locale.ENGLISH);
        return delegate.get(key);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> keys() {
        return new Vector(originalKeys).elements();
    }

    @Override
    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public Set keySet() {
        return originalKeys;
    }

    @Override
    public Set entrySet() {
        Set<Map.Entry> entrySet = new HashSet<Map.Entry>();
        for (final String key : originalKeys) {
            final Object value = get(key);
            Map.Entry entry = new Map.Entry() {

                @Override
                public Object getKey() {
                    return key;
                }

                @Override
                public Object getValue() {
                    return value;
                }

                @Override
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException("immutable");
                }
            };
            entrySet.add(entry);
        }
        return entrySet;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
