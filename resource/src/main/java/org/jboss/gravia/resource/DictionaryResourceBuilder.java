/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.jboss.gravia.resource;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

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
        boolean headerFound = false;
        Enumeration<String> keys = headers.keys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            String value = headers.get(name);
            if (name.startsWith("Gravia-") || name.equals(Constants.MODULE_ACTIVATOR)) {
                headerFound = true;
            }
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
        if (headerFound && !identityFound) {
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
