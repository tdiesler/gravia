package org.jboss.gravia.resource;

/**
 * Adds attachment support
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Attachable  {

    /**
     * Attach an arbirtary object with this element.
     *
     * @param key key for the attachment
     * @return The previously attachment object or null
     */
    <T> T putAttachment(AttachmentKey<T> key, T value);

    /**
     * Get the attached object for a given key
     *
     * @param type key for the attachment
     * @return The attached object or null
     */
    <T> T getAttachment(AttachmentKey<T> key);

    /**
     * Remove an attached object for a given key
     *
     * @param clazz key for the attachment
     * @return The attached object or null
     */
    <T> T removeAttachment(AttachmentKey<T> key);
}
