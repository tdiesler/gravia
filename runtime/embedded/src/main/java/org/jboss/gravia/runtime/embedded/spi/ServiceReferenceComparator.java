/*
 * #%L
 * Gravia :: Runtime :: Embedded
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.runtime.embedded.spi;

import java.util.Comparator;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

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
