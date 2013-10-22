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
package org.jboss.gravia.runtime.embedded.osgi;

import static org.jboss.gravia.runtime.spi.AbstractRuntime.LOGGER;

import org.osgi.service.log.LogService;

/**
 * The internal log service
 *
 * @author thomas.diesler@jboss.com
 * @since 22-Oct-2013
 */
public final class EmbeddedLogService implements LogService {

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

    private void logInternal(int level, String message, Throwable cause, Object source) {
        if (source != null) {
            message = "[" + source + "] " + message;
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
