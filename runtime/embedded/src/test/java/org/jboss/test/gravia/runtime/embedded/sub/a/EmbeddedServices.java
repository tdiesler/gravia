/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
package org.jboss.test.gravia.runtime.embedded.sub.a;

import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

@Component(service = { EmbeddedServices.class }, immediate = true)
public class EmbeddedServices {

    final AtomicReference<LogService> logService = new AtomicReference<LogService>();
    final AtomicReference<ConfigurationAdmin> configAdmin = new AtomicReference<ConfigurationAdmin>();
    final AtomicReference<MBeanServer> mbeanServer = new AtomicReference<MBeanServer>();

    @Activate
    void activate(ComponentContext context) {
    }

    @Deactivate
    void deactivate() {
    }

    public LogService getLogService() {
        return logService.get();
    }

    public ConfigurationAdmin getConfigurationAdmin() {
        return configAdmin.get();
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer.get();
    }

    @Reference
    void bindMBeanServer(MBeanServer service) {
        mbeanServer.set(service);
    }

    void unbindMBeanServer(MBeanServer service) {
        mbeanServer.compareAndSet(service, null);
    }

    @Reference
    void bindConfigurationAdmin(ConfigurationAdmin service) {
        configAdmin.set(service);
    }

    void unbindConfigurationAdmin(ConfigurationAdmin service) {
        configAdmin.compareAndSet(service, null);
    }

    @Reference
    void bindLogService(LogService service) {
        logService.set(service);
    }

    void unbindLogService(LogService service) {
        logService.compareAndSet(service, null);
    }
}