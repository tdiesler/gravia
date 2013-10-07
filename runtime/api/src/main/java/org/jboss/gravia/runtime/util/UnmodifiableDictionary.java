package org.jboss.gravia.runtime.util;

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
        NotNullException.assertValue(props, "delegate");

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