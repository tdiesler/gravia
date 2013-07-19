package org.jboss.gravia.resolver;

import java.util.Comparator;
import java.util.List;

import org.jboss.gravia.resource.Capability;


/**
 * A preference policy for providers
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface PreferencePolicy {

    /**
     * Sort the given providers according to their preference
     */
    void sort(List<Capability> providers);
    
    /**
     * Get the capability comparator
     */
    Comparator<Capability> getComparator();
}
