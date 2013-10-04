/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.gravia.runtime.osgi.sub.a;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.osgi.OSGiRuntime;
import org.jboss.gravia.runtime.osgi.OSGiRuntimeLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A Service Activator
 *
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class SimpleActivator implements BundleActivator, ModuleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        Bundle bundle = context.getBundle();
        OSGiRuntime runtime = OSGiRuntimeLocator.locateRuntime(context);
        Module module = runtime.installModule(bundle);
        start(module.getModuleContext());
    }

    @Override
    public void stop(BundleContext context) {
    }

    @Override
    public void start(ModuleContext context) throws Exception {
        context.registerService(String.class, new String("Hello"), null);
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
    }
}