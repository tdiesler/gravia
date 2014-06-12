/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.repository.spi;

import java.util.HashMap;
import java.util.Map;

import org.jboss.gravia.repository.RepositoryWriter;


/**
 * An abstract content handler.
 *
 * @author thomas.diesler@jboss.com
 * @since 12-Jun-2012
 */
public abstract class AbstractContentHandler implements RepositoryWriter.ContentHandler {

    private final Map<Class<?>, Object> contextItems = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T addContextItem(Class<T> type, T obj) {
        return (T) contextItems.put(type, obj);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T removeContextItem(Class<T> type) {
        return (T) contextItems.remove(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getContextItem(Class<T> type) {
        return (T) contextItems.get(type);
    }
}
