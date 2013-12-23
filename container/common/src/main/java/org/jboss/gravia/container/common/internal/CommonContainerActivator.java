package org.jboss.gravia.container.common.internal;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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

import java.io.IOException;
import java.net.URL;

import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activate the {@link Repository} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class CommonContainerActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {

        Bundle bundle = context.getBundle();
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(bundle.getBundleId());
        ModuleContext syscontext = module.getModuleContext();

        ServiceReference<Repository> sref = syscontext.getServiceReference(Repository.class);
        if (sref == null)
            return;

        try {
            if (sref != null) {
                Repository repository = syscontext.getService(sref);
                for (URL path : syscontext.getModule().findEntries("META-INF/repository-content", "*.xml", false)) {
                    try {
                        RepositoryReader reader = new DefaultRepositoryXMLReader(path.openStream());
                        Resource auxres = reader.nextResource();
                        while (auxres != null) {
                            repository.addResource(auxres);
                            auxres = reader.nextResource();
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException("Cannot install feature to repository: " + path);
                    }
                }
            }
        } finally {
            syscontext.ungetService(sref);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
