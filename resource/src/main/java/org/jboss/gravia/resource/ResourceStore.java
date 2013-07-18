package org.jboss.gravia.resource;

import java.util.Iterator;
import java.util.Set;

/**
 * A resource store
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface ResourceStore  {

    Iterator<Resource> getResources();
    
    Resource addResource(Resource resource);
    
    Resource removeResource(ResourceIdentity identity);
    
    Resource getResource(ResourceIdentity identity);
    
    Set<Capability> findProviders(Requirement requirement);
    
}
