/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.runtime.util;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.jboss.gravia.utils.NotNullException;

/**
 * Provides Moduel headers from a manifest
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public final class ManifestHeadersProvider {

    private final Manifest manifest;

    public ManifestHeadersProvider(Manifest manifest) {
        NotNullException.assertValue(manifest, "manifest");
        this.manifest = manifest;
    }

    /**
     * Return a mutable dictionary of manifest headers
     */
    public Dictionary<String, String> getHeaders() {
        Hashtable<String, String> headers = new Hashtable<String, String>();
        Attributes mainAttributes = manifest.getMainAttributes();
        for (Object key : mainAttributes.keySet()) {
            Name name = (Name) key;
            String value = mainAttributes.getValue(name);
            headers.put(name.toString(), value);
        }
        return headers;
    }
}
