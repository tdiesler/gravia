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
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.gravia.Constants;
import org.jboss.gravia.resource.spi.ElementParser;

/**
 * Build a {@link Resource} from a given Dictionary.
 *
 * @author thomas.diesler@jboss.com
 * @since 03-Oct-2013
 *
 * @NotThreadSafe
 */
public class DictionaryResourceBuilder extends DefaultResourceBuilder {

    public DictionaryResourceBuilder load(Dictionary<String, String> headers) {
        boolean identityFound = false;
        Enumeration<String> keys = headers.keys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            String value = headers.get(name);
            if (Constants.GRAVIA_IDENTITY_CAPABILITY.equals(name)) {
                Map<String, Object> atts = new LinkedHashMap<String, Object>();
                Map<String, String> dirs = new LinkedHashMap<String, String>();
                String symbolicName = parseParameterizedValue(value, atts, dirs);
                addIdentityCapability(symbolicName, null, atts, dirs);
                identityFound = true;
            } else if (Constants.GRAVIA_IDENTITY_REQUIREMENT.equals(name)) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String symbolicName = parseParameterizedValue(part, atts, dirs);
                    addIdentityRequirement(symbolicName, null, atts, dirs);
                }
            } else if (Constants.GRAVIA_CAPABILITY.equals(name)) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String namespace = parseParameterizedValue(part, atts, dirs);
                    addCapability(namespace, atts, dirs);
                }
            } else if (Constants.GRAVIA_REQUIREMENT.equals(name)) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String namespace = parseParameterizedValue(part, atts, dirs);
                    addRequirement(namespace, atts, dirs);
                }
            }
        }

        // Derive the identity from OSGi headers
        if (!identityFound) {
            String symbolicName = null;
            Version version = null;
            keys = headers.keys();
            while (keys.hasMoreElements()) {
                String name = keys.nextElement();
                String value = headers.get(name);
                if (name.equals("Bundle-SymbolicName")) {
                    symbolicName = value;
                    int index = symbolicName.indexOf(';');
                    if (index > 0) {
                        symbolicName = symbolicName.substring(0, index);
                    }
                } else if (name.equals("Bundle-Version")) {
                    version = Version.parseVersion(value);
                }
            }
            if (symbolicName != null) {
                addIdentityCapability(symbolicName, version);
            }
        }

        return this;
    }
}
