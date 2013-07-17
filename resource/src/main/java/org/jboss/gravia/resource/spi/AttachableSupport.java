/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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


/**
 * An implementation of {@link Attachable}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AttachableSupport implements Attachable {

    private Map<AttachmentKey<?>, Object> attachments;

    @Override
    public synchronized <T> T putAttachment(AttachmentKey<T> clazz, T value) {
        if (attachments == null)
            attachments = new HashMap<AttachmentKey<?>, Object>();

        @SuppressWarnings("unchecked")
        T result = (T) attachments.get(clazz);
        attachments.put(clazz, value);
        return result;
    }

    @Override
    public synchronized <T> T getAttachment(AttachmentKey<T> clazz) {
        if (attachments == null)
            return null;

        @SuppressWarnings("unchecked")
        T result = (T) attachments.get(clazz);
        return result;
    }

    @Override
    public synchronized <T> T removeAttachment(AttachmentKey<T> clazz) {
        if (attachments == null)
            return null;

        @SuppressWarnings("unchecked")
        T result = (T) attachments.remove(clazz);
        return result;
    }
}