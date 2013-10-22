/*
 * #%L
 * JBossOSGi Resolver Metadata
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.gravia.utils;

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
 * A case insensitive dictionary.
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
