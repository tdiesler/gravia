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
