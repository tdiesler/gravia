package org.jboss.gravia.resource;

/**
 * An attachment key
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AttachmentKey<T> {

    private final Class<T> type;

    public static <T> AttachmentKey<T> create(Class<T> type) {
        return new AttachmentKey<T>(type);
    }

    private AttachmentKey(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }
}
