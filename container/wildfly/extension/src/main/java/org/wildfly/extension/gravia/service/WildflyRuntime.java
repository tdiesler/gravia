/*
 * #%L
 * Gravia :: Integration Tests :: Common
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.wildfly.extension.gravia.service;

import java.util.Dictionary;

import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * The WildFly {@link Runtime}
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class WildflyRuntime extends EmbeddedRuntime {

    public static AttachmentKey<ResourceRoot> DEPLOYMENT_ROOT_KEY = AttachmentKey.create(ResourceRoot.class);

    public WildflyRuntime(PropertiesProvider propertiesProvider, Attachable context) {
        super(propertiesProvider, context);
    }

    @Override
    public AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context) {
        AbstractModule module = super.createModule(classLoader, resource, headers, context);
        ResourceRoot resourceRoot = context.getAttachment(WildflyRuntime.DEPLOYMENT_ROOT_KEY);
        if (resourceRoot != null) {
            ModuleEntriesProvider entriesProvider = new VirtualFileEntriesProvider(resourceRoot);
            module.putAttachment(AbstractModule.MODULE_ENTRIES_PROVIDER_KEY, entriesProvider);
        }
        return module;
    }
}
