/*
 * #%L
 * JBossOSGi Framework
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
package org.jboss.gravia.runtime.embedded.internal;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ServiceFactory;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The internal log service
 *
 * @author thomas.diesler@jboss.com
 * @since 22-Oct-2013
 */
final class EmbeddedLogServiceFactory implements ServiceFactory<LogService> {

    @Override
    public LogService getService(Module module, ServiceRegistration<LogService> registration) {
        return new EmbeddedLogService(module);
    }

    @Override
    public void ungetService(Module module, ServiceRegistration<LogService> registration, LogService service) {
        // nothing to do
    }

    static class EmbeddedLogService implements LogService {

        private final Logger LOGGER;

        EmbeddedLogService(Module module) {
            LOGGER = LoggerFactory.getLogger(module.getIdentity().toString());
        }

        @Override
        public void log(int level, String message) {
            logInternal(level, message, null, null);
        }

        @Override
        public void log(int level, String message, Throwable cause) {
            logInternal(level, message, cause, null);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void log(org.osgi.framework.ServiceReference sref, int level, String message) {
            logInternal(level, message, null, sref);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void log(org.osgi.framework.ServiceReference sref, int level, String message, Throwable cause) {
            logInternal(level, message, cause, sref);
        }

        private void logInternal(int level, String message, Throwable cause, Object sref) {
            if (sref != null) {
                message = "[" + sref + "] " + message;
            }
            switch (level) {
            case LOG_DEBUG:
                LOGGER.debug(message, cause);
                break;
            case LOG_INFO:
                LOGGER.info(message, cause);
                break;
            case LOG_WARNING:
                LOGGER.warn(message, cause);
                break;
            case LOG_ERROR:
                LOGGER.error(message, cause);
                break;
            }
        }
    }
}
