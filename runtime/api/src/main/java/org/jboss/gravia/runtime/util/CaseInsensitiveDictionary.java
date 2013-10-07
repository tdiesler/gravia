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
package org.jboss.gravia.runtime.util;

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
 * @author Thomas.Diesler@jboss.com
 */
@SuppressWarnings("rawtypes")
public class CaseInsensitiveDictionary<V> extends Hashtable<String, V> {
    private static final long serialVersionUID = 5802491129524016545L;

    /** The delegate dictionary */
    private Dictionary<String, V> delegate;

    /** The original keys */
    private Set<String> originalKeys;

    /**
     * Create a new CaseInsensitiveDictionary.
     *
     * @param delegate the delegate
     */
    public CaseInsensitiveDictionary(Dictionary<String, V> delegate) {
        NotNullException.assertValue(delegate, "delegate");

        this.delegate = new Hashtable<String, V>(delegate.size());
        this.originalKeys = Collections.synchronizedSet(new HashSet<String>());
        Enumeration<String> e = delegate.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            if (get(key) != null)
                throw new IllegalArgumentException("Duplicates for key [" + key + "] in: " + delegate);

            this.delegate.put(key.toLowerCase(Locale.ENGLISH), delegate.get(key));
            originalKeys.add(key);
        }
    }

    @Override
    public Enumeration<V> elements() {
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
    public V get(Object key) {
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
    public V put(String key, V value) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public Set<String> keySet() {
        return originalKeys;
    }

    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        Set<Map.Entry<String, V>> entrySet = new HashSet<Map.Entry<String, V>>();
        for (final String key : originalKeys) {
            final V value = get(key);
            Map.Entry<String, V> entry = new Map.Entry<String, V>() {

                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public V getValue() {
                    return value;
                }

                @Override
                public V setValue(Object value) {
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
