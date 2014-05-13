/*
 * #%L
 * Gravia :: Resource
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.utils;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An unmodifiable dictionary.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Dec-2009
 */
public class UnmodifiableDictionary<K, V> extends Dictionary<K, V> implements Serializable {

    private static final long serialVersionUID = -6793757957920326746L;

    private final Dictionary<K, V> delegate;

    public UnmodifiableDictionary(Dictionary<K, V> props) {
        ArgumentAssertion.assertNotNull(props, "delegate");

        delegate = new Hashtable<K, V>();
        Enumeration<K> keys = props.keys();
        while (keys.hasMoreElements()) {
            K key = keys.nextElement();
            V val = props.get(key);
            delegate.put(key, val);
        }
    }

    @Override
    public Enumeration<V> elements() {
        return delegate.elements();
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Enumeration<K> keys() {
        return delegate.keys();
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
