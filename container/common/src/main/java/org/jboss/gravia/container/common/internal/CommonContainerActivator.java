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
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.runtime.DefaultBundleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;

/**
 * Activate the {@link Repository} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class CommonContainerActivator extends DefaultBundleActivator {

    @Override
    public void start(final ModuleContext context) throws Exception {
        ServiceReference<Repository> sref = context.getServiceReference(Repository.class);
        if (sref == null)
            return;

        try {
            if (sref != null) {
                Repository repository = context.getService(sref);
                for (URL path : context.getModule().findEntries("META-INF/repository-content", "*.xml", false)) {
                    try {
                        RepositoryReader reader = new DefaultRepositoryXMLReader(path.openStream());
                        org.jboss.gravia.resource.Resource auxres = reader.nextResource();
                        while (auxres != null) {
                            RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
                            if (storage.getResource(auxres.getIdentity()) == null) {
                                storage.addResource(auxres);
                            }
                            auxres = reader.nextResource();
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException("Cannot install feature to repository: " + path);
                    }
                }
            }
        } finally {
            context.ungetService(sref);
        }
    }
}
