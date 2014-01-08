/*
 * #%L
 * Wildfly Gravia Subsystem
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


package org.wildfly.extension.gravia;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.msc.service.ServiceName;

/**
 * Gravia subsystem constants.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 22-Apr-2013
 */
public interface GraviaConstants {

    /** The base name for all gravia services */
    ServiceName GRAVIA_BASE_NAME = ServiceName.JBOSS.append("wildfly", "gravia");
    /** The name for the gravia subsystem service */
    ServiceName GRAVIA_SUBSYSTEM_SERVICE_NAME = GRAVIA_BASE_NAME.append("Subsystem");
    /** The name for the {@link Environment} service */
    ServiceName ENVIRONMENT_SERVICE_NAME = GRAVIA_BASE_NAME.append("Environment");
    /** The name for the {@link ModuleContext} service */
    ServiceName MODULE_CONTEXT_SERVICE_NAME = GRAVIA_BASE_NAME.append("ModuleContext");
    /** The name for the {@link Provisioner} service */
    ServiceName PROVISIONER_SERVICE_NAME = GRAVIA_BASE_NAME.append("Provisioner");
    /** The name for the {@link Repository} service */
    ServiceName REPOSITORY_SERVICE_NAME = GRAVIA_BASE_NAME.append("Repository");
    /** The name for the {@link Resolver} service */
    ServiceName RESOLVER_SERVICE_NAME = GRAVIA_BASE_NAME.append("Resolver");
    /** The name for the {@link ResourceInstaller} service */
    ServiceName RESOURCE_INSTALLER_SERVICE_NAME = GRAVIA_BASE_NAME.append("ResourceInstaller");
    /** The name for the {@link Runtime} service */
    ServiceName RUNTIME_SERVICE_NAME = GRAVIA_BASE_NAME.append("Runtime");

    /** The deployment names for repository content deployments */
    String REPOSITORY_CONTENT_FILE_SUFFIX = "-repository-content.xml";
    String REPOSITORY_CONTENT_FILE_NAME = "META-INF/jboss-repository-content.xml";

    /** The {@link Repository} attachment key */
    AttachmentKey<Repository> REPOSITORY_KEY = AttachmentKey.create(Repository.class);
    /** The {@link Resource} attachment key */
    AttachmentKey<Resource> RESOURCE_KEY = AttachmentKey.create(Resource.class);
    /** The {@link Module} attachment key */
    AttachmentKey<Module> MODULE_KEY = AttachmentKey.create(Module.class);
}
