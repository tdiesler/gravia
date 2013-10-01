/*
 * #%L
 * JBossOSGi Framework
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
package org.jboss.gravia.runtime;

import java.util.Dictionary;
import java.util.Map;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface Filter {

    /**
     * Filter using a service's properties.
     * <p>
     * This {@code Filter} is executed using the keys and values of the
     * referenced service's properties. The keys are looked up in a case
     * insensitive manner.
     * 
     * @param reference The reference to the service whose properties are used
     *        in the match.
     * @return {@code true} if the service's properties match this
     *         {@code Filter}; {@code false} otherwise.
     */
    boolean match(ServiceReference<?> reference);

    /**
     * Filter using a {@code Dictionary} with case insensitive key lookup. This
     * {@code Filter} is executed using the specified {@code Dictionary}'s keys
     * and values. The keys are looked up in a case insensitive manner.
     * 
     * @param dictionary The {@code Dictionary} whose key/value pairs are used
     *        in the match.
     * @return {@code true} if the {@code Dictionary}'s values match this
     *         filter; {@code false} otherwise.
     * @throws IllegalArgumentException If {@code dictionary} contains case
     *         variants of the same key name.
     */
    boolean match(Dictionary<String, ?> dictionary);

    /**
     * Returns this {@code Filter}'s filter string.
     * <p>
     * The filter string is normalized by removing whitespace which does not
     * affect the meaning of the filter.
     * 
     * @return This {@code Filter}'s filter string.
     */
    @Override
    String toString();

    /**
     * Compares this {@code Filter} to another {@code Filter}.
     * 
     * <p>
     * This implementation returns the result of calling
     * {@code this.toString().equals(obj.toString())}.
     * 
     * @param obj The object to compare against this {@code Filter}.
     * @return If the other object is a {@code Filter} object, then returns the
     *         result of calling {@code this.toString().equals(obj.toString())};
     *         {@code false} otherwise.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns the hashCode for this {@code Filter}.
     * 
     * <p>
     * This implementation returns the result of calling
     * {@code this.toString().hashCode()}.
     * 
     * @return The hashCode of this {@code Filter}.
     */
    @Override
    int hashCode();

    /**
     * Filter using a {@code Dictionary}. This {@code Filter} is executed using
     * the specified {@code Dictionary}'s keys and values. The keys are looked
     * up in a normal manner respecting case.
     * 
     * @param dictionary The {@code Dictionary} whose key/value pairs are used
     *        in the match.
     * @return {@code true} if the {@code Dictionary}'s values match this
     *         filter; {@code false} otherwise.
     */
    boolean matchCase(Dictionary<String, ?> dictionary);

    /**
     * Filter using a {@code Map}. This {@code Filter} is executed using the
     * specified {@code Map}'s keys and values. The keys are looked up in a
     * normal manner respecting case.
     * 
     * @param map The {@code Map} whose key/value pairs are used in the match.
     *        Maps with {@code null} key or values are not supported. A
     *        {@code null} value is considered not present to the filter.
     * @return {@code true} if the {@code Map}'s values match this filter;
     *         {@code false} otherwise.
     */
    boolean matches(Map<String, ?> map);
}
