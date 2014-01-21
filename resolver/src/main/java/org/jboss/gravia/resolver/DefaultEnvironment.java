/*
 * #%L
 * Gravia :: Resolver
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
package org.jboss.gravia.resolver;

import java.util.Iterator;

import org.jboss.gravia.resolver.spi.AbstractEnvironment;
import org.jboss.gravia.resource.DefaultMatchPolicy;
import org.jboss.gravia.resource.Resource;

/**
 * The default {@link Environment}
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public class DefaultEnvironment extends AbstractEnvironment {

    public DefaultEnvironment(String envname) {
        super(envname, new DefaultMatchPolicy());
    }

    @Override
    public Environment cloneEnvironment() {
        DefaultEnvironment result = new DefaultEnvironment("Cloned " + getName());
        Iterator<Resource> itres = getResources();
        while (itres.hasNext()) {
            result.addResource(itres.next());
        }
        return result;
    }
}
