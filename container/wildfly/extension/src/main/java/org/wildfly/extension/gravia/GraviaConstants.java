/*
 * #%L
 * Gravia :: Container :: WildFly :: Extension
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


package org.wildfly.extension.gravia;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Environment;
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
    /** The {@link Runtime} attachment key */
    AttachmentKey<Runtime> RUNTIME_KEY = AttachmentKey.create(Runtime.class);
    /** The {@link Resource} attachment key */
    AttachmentKey<Resource> RESOURCE_KEY = AttachmentKey.create(Resource.class);
    /** The {@link Module} attachment key */
    AttachmentKey<Module> MODULE_KEY = AttachmentKey.create(Module.class);
}
