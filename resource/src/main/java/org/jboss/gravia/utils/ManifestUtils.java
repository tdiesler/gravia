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
package org.jboss.gravia.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * A utility class for Manifest operations.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-Sep-2010
 */
public final class ManifestUtils {

    // Hide ctor
    private ManifestUtils() {
    }

    public static Manifest getManifest(InputStream input) {
        JarInputStream jarStream;
        if (input instanceof JarInputStream) {
            jarStream = (JarInputStream) input;
        } else {
            try {
                jarStream = new JarInputStream(input);
            } catch (IOException ex) {
                return null;
            }
        }
        try {
            return jarStream.getManifest();
        } finally {
            IOUtils.safeClose(jarStream);
        }
    }

    public static Manifest getManifest(File file) {
        JarFile jarFile;
        try {
            jarFile = new JarFile(file);
        } catch (IOException ex) {
            return null;
        }
        try {
            return jarFile.getManifest();
        } catch (IOException ex) {
            return null;
        } finally {
            IOUtils.safeClose(jarFile);
        }
    }

    public static Dictionary<String, String> getManifestHeaders(Manifest manifest) {
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
