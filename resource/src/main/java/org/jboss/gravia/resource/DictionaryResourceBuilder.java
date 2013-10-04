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
 */
public final class DictionaryResourceBuilder extends DefaultResourceBuilder {

    public DictionaryResourceBuilder load(Dictionary<String, String> headers) {
        Enumeration<String> keys = headers.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = headers.get(key);
            if (ManifestBuilder.GRAVIA_IDENTITY_CAPABILITY.equals(key)) {
                Map<String, Object> atts = new LinkedHashMap<String, Object>();
                Map<String, String> dirs = new LinkedHashMap<String, String>();
                String symbolicName = parseParameterizedValue(value, atts, dirs);
                addIdentityCapability(symbolicName, null, atts, dirs);
            } else if (ManifestBuilder.GRAVIA_IDENTITY_REQUIREMENT.equals(key)) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String symbolicName = parseParameterizedValue(part, atts, dirs);
                    addIdentityRequirement(symbolicName, null, atts, dirs);
                }
            } else if (ManifestBuilder.GRAVIA_CAPABILITY.equals(key)) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String namespace = parseParameterizedValue(part, atts, dirs);
                    addCapability(namespace, atts, dirs);
                }
            } else if (ManifestBuilder.GRAVIA_REQUIREMENT.equals(key)) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String namespace = parseParameterizedValue(part, atts, dirs);
                    addRequirement(namespace, atts, dirs);
                }
            }
        }
        return this;
    }
}
