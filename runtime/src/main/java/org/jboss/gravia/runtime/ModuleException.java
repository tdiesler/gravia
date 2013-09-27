package org.jboss.gravia.runtime;

public class ModuleException extends Exception {

    private static final long serialVersionUID = 1L;

    public ModuleException(String message) {
        super(message);
    }

    public ModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleException(Throwable cause) {
        super(cause);
    }
}
