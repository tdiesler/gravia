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

import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.resource.Resource;
import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * Logging Id ranges: 20100-20199
 *
 * https://community.jboss.org/wiki/LoggingIds
 *
 * @author Thomas.Diesler@jboss.com
 * @since 22-Apr-2013
 */
@MessageBundle(projectCode = "JBAS")
public interface GraviaMessages {

    /**
     * The messages.
     */
    GraviaMessages MESSAGES = Messages.getBundle(GraviaMessages.class);

    @Message(id = 20100, value = "%s is null")
    IllegalArgumentException illegalArgumentNull(String name);

    @Message(id = 20111, value = "Cannot install feature to repository: %s")
    IllegalStateException cannotInstallGraviaFeature(String name);

    @Message(id = 20112, value = "Cannot install resource to environment: %s")
    IllegalStateException cannotInstallResourceToEnvironment(String name);

    @Message(id = 20114, value = "Cannot create repository storage area")
    IllegalStateException cannotCreateRepositoryStorageArea(@Cause Throwable th);

    @Message(id = 20115, value = "Cannot provision resource: %s")
    ProvisionException cannotProvisionResource(@Cause Throwable th, Resource res);

    @Message(id = 20116, value = "Cannot uninstall provisioned resource: %s")
    ProvisionException cannotUninstallProvisionedResource(@Cause Throwable th, Resource res);
}

