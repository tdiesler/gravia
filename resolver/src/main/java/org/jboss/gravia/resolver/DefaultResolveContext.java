/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

package org.jboss.gravia.resolver;

import java.util.Set;

import org.jboss.gravia.resolver.spi.AbstractResolveContext;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceStore;

/**
 * The default {@link ResolveContext}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public class DefaultResolveContext extends AbstractResolveContext {
    
    public DefaultResolveContext(ResourceStore resourceStore, Set<Resource> mandatory, Set<Resource> optional) {
        super(resourceStore, mandatory, optional);
    }

    @Override
    protected PreferencePolicy createPreferencePolicy() {
        return new DefaultPreferencePolicy(getWirings());
    }
}
