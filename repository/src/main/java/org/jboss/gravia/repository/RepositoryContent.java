/*
 * Copyright (c) OSGi Alliance (2012). All Rights Reserved.
 * 
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
 */

package org.jboss.gravia.repository;

import java.io.InputStream;

import org.jboss.gravia.resource.Resource;

/**
 * An accessor for the default content of a resource.
 * 
 * All {@link Resource} objects which represent non-abstract resources in a
 * {@link Repository} must be adaptable to this interface.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2012
 */
public interface RepositoryContent {

    /**
     * Returns a new input stream to the default format of this resource.
     * 
     * @return A new input stream for associated resource.
     */
    InputStream getContent();
}
