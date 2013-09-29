/*
 * #%L
 * Gravia :: Runtime :: API
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
package org.jboss.gravia.runtime;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class ServiceException extends RuntimeException {
    static final long       serialVersionUID    = 3038963223712959631L;

    /**
     * Type of service exception.
     */
    private final int       type;

    /**
     * No exception type is unspecified.
     */
    public static final int UNSPECIFIED         = 0;
    /**
     * The service has been unregistered.
     */
    public static final int UNREGISTERED        = 1;
    /**
     * The service factory produced an invalid service object.
     */
    public static final int FACTORY_ERROR       = 2;
    /**
     * The service factory threw an exception.
     */
    public static final int FACTORY_EXCEPTION   = 3;
    /**
     * The exception is a subclass of ServiceException. The subclass should be
     * examined for the type of the exception.
     */
    public static final int SUBCLASSED          = 4;
    /**
     * An error occurred invoking a remote service.
     */
    public static final int REMOTE              = 5;
    /**
     * The service factory resulted in a recursive call to itself for the
     * requesting bundle.
     * 
     * @since 1.6
     */
    public static final int FACTORY_RECURSION   = 6;

    /**
     * Creates a {@code ServiceException} with the specified message and
     * exception cause.
     * 
     * @param msg The associated message.
     * @param cause The cause of this exception.
     */
    public ServiceException(String msg, Throwable cause) {
        this(msg, UNSPECIFIED, cause);
    }

    /**
     * Creates a {@code ServiceException} with the specified message.
     * 
     * @param msg The message.
     */
    public ServiceException(String msg) {
        this(msg, UNSPECIFIED);
    }

    /**
     * Creates a {@code ServiceException} with the specified message, type and
     * exception cause.
     * 
     * @param msg The associated message.
     * @param type The type for this exception.
     * @param cause The cause of this exception.
     */
    public ServiceException(String msg, int type, Throwable cause) {
        super(msg, cause);
        this.type = type;
    }

    /**
     * Creates a {@code ServiceException} with the specified message and type.
     * 
     * @param msg The message.
     * @param type The type for this exception.
     */
    public ServiceException(String msg, int type) {
        super(msg);
        this.type = type;
    }

    /**
     * Returns the type for this exception or {@code UNSPECIFIED} if the type
     * was unspecified or unknown.
     * 
     * @return The type of this exception.
     */
    public int getType() {
        return type;
    }
}
