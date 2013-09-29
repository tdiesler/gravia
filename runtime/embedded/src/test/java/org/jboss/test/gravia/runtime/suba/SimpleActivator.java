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
package org.jboss.test.gravia.runtime.suba;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A Service Activator
 *
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class SimpleActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) {
        context.registerService(String.class, new String("Hello"), null);
    }

    @Override
    public void stop(BundleContext context) {
    }
}
