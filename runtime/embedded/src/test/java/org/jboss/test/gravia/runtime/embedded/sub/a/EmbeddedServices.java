/*
 * #%L
 * Gravia :: Runtime :: Embedded
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
