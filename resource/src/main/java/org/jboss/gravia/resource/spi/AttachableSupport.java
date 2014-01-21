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
package org.jboss.gravia.resource.spi;

import java.util.HashMap;
import java.util.Map;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.utils.NotNullException;


/**
 * An implementation of {@link Attachable}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AttachableSupport implements Attachable {

    private Map<AttachmentKey<?>, Object> attachments;

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T putAttachment(AttachmentKey<T> key, T value) {
        NotNullException.assertValue(key, "key");
        NotNullException.assertValue(value, "value");
        if (attachments == null) {
            attachments = new HashMap<AttachmentKey<?>, Object>();
        }
        return (T) attachments.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T getAttachment(AttachmentKey<T> key) {
        NotNullException.assertValue(key, "key");
        return attachments != null ? (T) attachments.get(key) : null;
    }

    @Override
    public synchronized <T> boolean hasAttachment(AttachmentKey<T> key) {
        NotNullException.assertValue(key, "key");
        return attachments != null ? attachments.containsKey(key) : false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T removeAttachment(AttachmentKey<T> key) {
        NotNullException.assertValue(key, "key");
        return attachments != null ? (T) attachments.remove(key) : null;
    }
}
