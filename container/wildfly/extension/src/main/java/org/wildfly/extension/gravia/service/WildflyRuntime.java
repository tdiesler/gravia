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

import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;
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
    protected ModuleEntriesProvider getDefaultEntriesProvider(Module module, Attachable context) {
        ResourceRoot resourceRoot = context.getAttachment(WildflyRuntime.DEPLOYMENT_ROOT_KEY);
        return resourceRoot != null ? new VirtualFileEntriesProvider(resourceRoot) : null;
    }
}
