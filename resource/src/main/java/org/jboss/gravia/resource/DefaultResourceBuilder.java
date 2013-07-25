/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file ecept in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either epress or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.gravia.resource;

import java.util.Map;

import org.jboss.gravia.resource.spi.AbstractCapability;
import org.jboss.gravia.resource.spi.AbstractRequirement;
import org.jboss.gravia.resource.spi.AbstractResource;
import org.jboss.gravia.resource.spi.AbstractResourceBuilder;


/**
 * The default {@link Resource} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class DefaultResourceBuilder extends AbstractResourceBuilder {

    @Override
    protected AbstractResource createResource() {
        return new DefaultResource();
    }

    @Override
    protected AbstractCapability createCapability(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives) {
        return new DefaultCapability(resource, namespace, attributes, directives);
    }

    @Override
    protected AbstractRequirement createRequirement(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives) {
        return new DefaultRequirement(resource, namespace, attributes, directives);
    }

}
