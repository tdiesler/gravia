/*
 * #%L
 * Gravia :: Resource
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
     * @return The previously attachment object or null
     */
    <T> T putAttachment(AttachmentKey<T> key, T value);

    /**
     * True if there is an attached object for a given key
     */
    <T> boolean hasAttachment(AttachmentKey<T> key);

    /**
     * Get the attached object for a given key
     * @return The attached object or null
     */
    <T> T getAttachment(AttachmentKey<T> key);

    /**
     * Remove an attached object for a given key
     * @return The attached object or null
     */
    <T> T removeAttachment(AttachmentKey<T> key);
}
