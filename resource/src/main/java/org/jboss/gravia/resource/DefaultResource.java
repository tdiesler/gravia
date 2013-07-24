package org.jboss.gravia.resource;

import java.util.Map;

import org.jboss.gravia.resource.spi.AbstractCapability;
import org.jboss.gravia.resource.spi.AbstractRequirement;
import org.jboss.gravia.resource.spi.AbstractResource;

/**
 * The default {@link Resource}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class DefaultResource extends AbstractResource {

    @Override
    protected AbstractCapability createCapability(String namespace, Map<String, Object> attributes, Map<String, String> directives) {
        return new DefaultCapability(this, namespace, attributes, directives);
    }

    @Override
    protected AbstractRequirement createRequirement(String namespace, Map<String, Object> attributes, Map<String, String> directives) {
        return new DefaultRequirement(this, namespace, attributes, directives);
    }

    @Override
    protected String getSimpleTypeName() {
        return getClass() == DefaultResource.class ? Resource.class.getSimpleName() : super.getSimpleTypeName();
    }
}
