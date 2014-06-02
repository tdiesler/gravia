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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.jboss.gravia.utils.IOUtils;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * Provides Module headers from properties
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public final class PropertiesHeadersProvider implements HeadersProvider {

    private final Dictionary<String, String> headers;

    public PropertiesHeadersProvider(File file) throws IOException {
        IllegalArgumentAssertion.assertNotNull(file, "file");
        InputStream input = new FileInputStream(file);
        try {
            Properties props = new Properties();
            props.load(input);
            headers = fromProperties(props);
        } finally {
            IOUtils.safeClose(input);
        }
    }

    public PropertiesHeadersProvider(InputStream input) throws IOException {
        IllegalArgumentAssertion.assertNotNull(input, "input");
        try {
            Properties props = new Properties();
            props.load(input);
            headers = fromProperties(props);
        } finally {
            IOUtils.safeClose(input);
        }
    }

    public PropertiesHeadersProvider(Properties props) {
        headers = fromProperties(props);
    }

    public Dictionary<String, String> getHeaders() {
        return headers;
    }

    private Dictionary<String, String> fromProperties(Properties props) {
        Dictionary<String, String> headers = new Hashtable<>();
        IllegalArgumentAssertion.assertNotNull(props, "props");
        for (String key : props.stringPropertyNames()) {
            headers.put(key, props.getProperty(key));
        }
        return headers;
    }
}
