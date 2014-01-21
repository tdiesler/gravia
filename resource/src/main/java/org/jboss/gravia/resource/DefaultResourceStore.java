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

import org.jboss.gravia.resource.spi.AbstractResourceStore;

/**
 * The default {@link ResourceStore}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class DefaultResourceStore extends AbstractResourceStore {

    public DefaultResourceStore(String storeName) {
        super(storeName, new DefaultMatchPolicy());
    }

    public DefaultResourceStore(String storeName, MatchPolicy matchPolicy) {
        super(storeName, matchPolicy);
    }
}
