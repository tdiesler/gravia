package org.jboss.gravia.resource;

import org.jboss.gravia.resource.spi.AbstractResource;

/**
 * The default {@link Resource}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class DefaultResource extends AbstractResource {

    @Override
    protected String getSimpleTypeName() {
        return getClass() == DefaultResource.class ? Resource.class.getSimpleName() : super.getSimpleTypeName();
    }
}
