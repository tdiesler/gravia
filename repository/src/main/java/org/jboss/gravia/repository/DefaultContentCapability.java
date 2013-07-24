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

package org.jboss.gravia.repository;

import java.util.Map;

import org.jboss.gravia.repository.spi.AbstractContentCapability;
import org.jboss.gravia.resource.spi.AbstractResource;

/**
 * The default implementation of a {@link ContentCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 05-Jul-2012
 */
public class DefaultContentCapability extends AbstractContentCapability {

    public DefaultContentCapability(AbstractResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        super(resource, namespace, atts, dirs);
    }
}
