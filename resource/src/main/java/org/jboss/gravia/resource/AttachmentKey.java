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
