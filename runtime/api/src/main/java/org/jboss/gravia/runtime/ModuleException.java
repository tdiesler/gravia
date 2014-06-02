/*
 * #%L
 * Gravia :: Runtime :: API
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
package org.jboss.gravia.runtime;

/**
 * A checked exception used to indicate that a module lifecycle problem
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
     * @param message The message.
     */
    public ModuleException(String message) {
        super(message);
    }

    /**
     * Creates a {@code ModuleException} with the specified message and
     * exception cause.
     *
     * @param message The associated message.
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
