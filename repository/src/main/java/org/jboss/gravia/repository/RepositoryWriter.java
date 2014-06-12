package org.jboss.gravia.repository;
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

import java.io.IOException;
import java.util.Map;

import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.Resource;

/**
 * Write repository contnet.
 *
 * @author thomas.diesler@jboss.com
 * @since 21-May-2012
 */
public interface RepositoryWriter {

    public interface ContentHandler {
    
        Map<String, Object> process(ContentCapability ccap) throws IOException;
    
        <T> T addContextItem(Class<T> type, T obj);
    
        <T> T removeContextItem(Class<T> type);
    
        <T> T getContextItem(Class<T> type);
    }

    void writeRepositoryElement(Map<String, String> attributes);

    void writeResource(Resource resource);

    void close();
}
