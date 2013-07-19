package org.jboss.gravia.resource;

import java.util.List;

/**
 * A resource is associated with Capabilities/Requirements.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Resource extends Adaptable, Attachable {

    ResourceIdentity getIdentity();

    Capability getIdentityCapability();
    
	List<Capability> getCapabilities(String namespace);

	List<Requirement> getRequirements(String namespace);
	
	Wiring getWiring();
}
