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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.spi.AttributeValueHandler;
import org.jboss.gravia.resource.spi.AttributeValueHandler.AttributeValue;
import org.jboss.gravia.resource.spi.ElementParser;

/**
 * The default {@link Resource} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class ManifestResourceBuilder extends DefaultResourceBuilder {

    public ManifestResourceBuilder load(Manifest manifest) {
        Attributes mainAttributes = manifest.getMainAttributes();
        for (Object key : mainAttributes.keySet()) {
            Attributes.Name name = (Name) key;
            String value = mainAttributes.getValue(name);
            if (ManifestBuilder.GRAVIA_IDENTITY_CAPABILITY.equals(name.toString())) {
                Map<String, Object> atts = new LinkedHashMap<String, Object>();
                Map<String, String> dirs = new LinkedHashMap<String, String>();
                String symbolicName = parseParameterizedValue(value, atts, dirs);
                addIdentityCapability(symbolicName, null, atts, dirs);
            } else if (ManifestBuilder.GRAVIA_IDENTITY_REQUIREMENT.equals(name.toString())) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String symbolicName = parseParameterizedValue(part, atts, dirs);
                    addIdentityRequirement(symbolicName, null, atts, dirs);
                }
            } else if (ManifestBuilder.GRAVIA_CAPABILITY.equals(name.toString())) {
                for(String part : ElementParser.parseDelimitedString(value, ',')) {
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    String namespace = parseParameterizedValue(part, atts, dirs);
                    addCapability(namespace, atts, dirs);
                }
            } else if (ManifestBuilder.GRAVIA_REQUIREMENT.equals(name.toString())) {
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

    private String parseParameterizedValue(String line, Map<String, Object> atts, Map<String, String> dirs) {
        String mainvalue = null;
        for (String part : ElementParser.parseDelimitedString(line, ';', true)) {
            if (part.indexOf(":=") > 0) {
                int index = part.indexOf(":=");
                String key = part.substring(0, index);
                String value = unquote(part.substring(index + 2));
                dirs.put(key.trim(), value);
            } else if (part.indexOf('=') > 0) {
                int index = part.indexOf('=');
                String keystr = part.substring(0, index);
                Object value = getAttributeValue(keystr, part.substring(index + 1));
                atts.put(getAttributeKey(keystr), value);
            } else if (mainvalue == null) {
                mainvalue = part;
            } else {
                throw new IllegalArgumentException("Cannot parse: " + line);
            }
        }
        return mainvalue;
    }

    private String getAttributeKey(String keystr) {
        String[] parts = keystr.split(":");
        return parts[0].trim();
    }

    private Object getAttributeValue(String key, String valstr) {
        String[] parts = key.split(":");
        if (parts.length == 1) {
            return unquote(valstr);
        }
        String typespec = parts[1].trim();
        if (typespec.startsWith("List")) {
            parts = typespec.split("[<>]");
            typespec = "List<" + (parts.length > 1 ? parts[1].trim() : "String") + ">";
        }
        AttributeValue attval = AttributeValueHandler.readAttributeValue(typespec, unquote(valstr));
        return attval.getValue();
    }

    private String unquote(String value) {
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }
}
