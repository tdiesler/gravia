/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
