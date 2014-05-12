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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.gravia.utils.NotNullException;

/**
 * A manifest {@link Resource} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 *
 * @NotThreadSafe
 */
public class ManifestResourceBuilder extends DictionaryResourceBuilder {

    public ManifestResourceBuilder load(Manifest manifest) {
        NotNullException.assertValue(manifest, "manifest");
        Dictionary<String, String> headers = getManifestHeaders(manifest);
        load(headers);
        return this;
    }

    private Dictionary<String, String> getManifestHeaders(Manifest manifest) {
        Hashtable<String, String> headers = new Hashtable<String, String>();
        Attributes mainatts = manifest.getMainAttributes();
        for (Object key : mainatts.keySet()) {
            String name = key.toString();
            String value = mainatts.getValue(name);
            headers.put(name, value);
        }
        return headers;
    }
}
