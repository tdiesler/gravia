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
package org.jboss.gravia.runtime.spi;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * A {@link org.jboss.gravia.runtime.spi.PropertiesProvider} that is applying placeholder substitution based on the property values of an external  {@link org.jboss.gravia.runtime.spi.PropertiesProvider}.
 */
public class SubstitutionPropertiesProvider implements PropertiesProvider {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9\\.\\-]+)}");
    private static final String BOX_FORMAT = "\\$\\{%s\\}";

    private final PropertiesProvider delegate;

    public SubstitutionPropertiesProvider(PropertiesProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getProperty(String key) {
        return getProperty(key, null);
    }

    @Override
	public Object getRequiredProperty(String key) {
        Object value = getProperty(key, null);
        IllegalStateAssertion.assertNotNull(value, "Cannot obtain property: " + key);
		return value;
	}
    
    @Override
    public Object getProperty(String key, Object defaultValue) {
        Object rawValue = delegate.getProperty(key, defaultValue);
        if (rawValue == null) {
            return defaultValue;
        } else {
            return substitute(String.valueOf(rawValue), delegate, new HashSet<String>());
        }
    }

    private static String substitute(String str, PropertiesProvider provider, Set<String> visited) {
        String result = str;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
        CopyOnWriteArraySet<String> copyOfVisited = new CopyOnWriteArraySet<>(visited);
        while (matcher.find()) {
            String name = matcher.group(1);
            String replacement = "";
            String toReplace = String.format(BOX_FORMAT, name);
            if (provider.getProperty(name) != null && !visited.contains(name)) {
                replacement = String.valueOf(provider.getProperty(name));
                replacement = replacement != null ? replacement : "";
                if (PLACEHOLDER_PATTERN.matcher(replacement).matches()) {
                    copyOfVisited.add(name);
                    replacement = substitute(replacement, provider, copyOfVisited);
                }
            }
            result = result.replaceAll(toReplace, Matcher.quoteReplacement(replacement));
        }
        return result;
    }
}
