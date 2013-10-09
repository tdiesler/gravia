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
package org.jboss.gravia.runtime.embedded.internal;

import java.util.Comparator;

import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.ServiceReference;


/**
 * If this ServiceReference and the specified ServiceReference have the same service id they are equal. This ServiceReference is
 * less than the specified ServiceReference if it has a lower service ranking and greater if it has a higher service ranking.
 *
 * Otherwise, if this ServiceReference and the specified ServiceReference have the same service ranking, this ServiceReference
 * is less than the specified ServiceReference if it has a higher service id and greater if it has a lower service id.
 *
 * @author thomas.diesler@jboss.com
 * @since 26-Jul-2010
 */
final class ServiceReferenceComparator implements Comparator<ServiceReference<?>> {

    private static final Comparator<ServiceReference<?>> INSTANCE = new ServiceReferenceComparator();

    static Comparator<ServiceReference<?>> getInstance() {
        return INSTANCE;
    }

    // Hide ctor
    private ServiceReferenceComparator() {
    }

    @Override
    public int compare(ServiceReference<?> ref1, ServiceReference<?> ref2) {
        long thisId = getProperty(ref1, Constants.SERVICE_ID);
        long otherId = getProperty(ref1, Constants.SERVICE_ID);

        // If this ServiceReference and the specified ServiceReference have the same service id they are equal
        if (thisId == otherId)
            return 0;

        // This ServiceReference is less than the specified ServiceReference if it has a lower service ranking
        // and greater if it has a higher service ranking.
        long thisRanking = getProperty(ref1, Constants.SERVICE_RANKING);
        long otherRanking = getProperty(ref2, Constants.SERVICE_RANKING);
        if (thisRanking != otherRanking)
            return thisRanking < otherRanking ? -1 : 1;

        // This ServiceReference is less than the specified ServiceReference if it has a higher service id
        // and greater if it has a lower service id
        return thisId > otherId ? -1 : 1;
    }

    private long getProperty(ServiceReference<?> sref, String property) {
        Object value = sref.getProperty(property);
        return value != null ? new Long(value.toString()) : 0;
    }
}
