/*
 * #%L
 * Gravia :: Runtime :: OSGi
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
package org.jboss.test.gravia.runtime.osgi.sub.a;

import org.jboss.gravia.runtime.DefaultBundleActivator;
import org.jboss.gravia.runtime.ModuleContext;

/**
 * A Service Activator
 *
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class SimpleBundleActivator extends DefaultBundleActivator {

    @Override
    public void start(ModuleContext context) throws Exception {
        context.registerService(String.class, new String("Hello"), null);
    }
}