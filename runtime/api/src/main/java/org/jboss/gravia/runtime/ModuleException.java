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
 * An xception used to indicate that a module lifecycle problem
 * occurred.
 *
 * <p>
 * A {@code ModuleException} object is created by the Runtime to denote an
 * exception condition in the lifecycle of a module. {@code ModuleException}s
 * should not be created by module developers.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class ModuleException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@code ModuleException} with the specified message.
     *
     * @param msg The message.
     */
    public ModuleException(String message) {
        super(message);
    }

    /**
     * Creates a {@code ModuleException} with the specified message and
     * exception cause.
     *
     * @param msg The associated message.
     * @param cause The cause of this exception.
     */
    public ModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a {@code ModuleException} with the specified exception cause.
     *
     * @param cause The cause of this exception.
     */
    public ModuleException(Throwable cause) {
        super(cause);
    }

    /**
     * Launders the given throwable
     *
     * <ul>
     * <li>Rethrows a given {@link RuntimeException}
     * <li>Rethrows a given {@link Error}
     * <li>Returns a given {@link ModuleException}
     * <li>Otherwise wrapps the given {@link Throwable} in a {@link ModuleException}
     * </ul>
     */
    public static ModuleException launderThrowable(Throwable cause) {
        if (cause instanceof RuntimeException)
            throw (RuntimeException) cause;
        if (cause instanceof Error)
            throw (Error) cause;
        if (cause instanceof ModuleException)
            return (ModuleException) cause;
        return new ModuleException(cause);
    }
}
