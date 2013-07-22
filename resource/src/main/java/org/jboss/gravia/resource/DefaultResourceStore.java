package org.jboss.gravia.resource;

import org.jboss.gravia.resource.spi.AbstractResourceStore;

/**
 * The default {@link ResourceStore}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class DefaultResourceStore extends AbstractResourceStore {

    public DefaultResourceStore(String storeName) {
        super(storeName);
    }

    public DefaultResourceStore(String storeName, boolean logCapsReqs) {
        super(storeName, logCapsReqs);
    }

    protected MatchPolicy createMatchPolicy() {
        return new DefaultMatchPolicy();
    }
}
