/*
 * #%L
 * JBossOSGi SPI
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.logging.Logger;

/**
 * A simple manifest builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-Mar-2010
 */
public final class ManifestBuilder {

    public static final String RESOURCE_IDENTITY_CAPABILITY = "Resource-Identity";
    public static final String RESOURCE_IDENTITY_REQUIREMENT = "Resource-IdentityRequirement";
    public static final String RESOURCE_CAPABILITY = "Resource-Capability";
    public static final String RESOURCE_REQUIREMENT = "Resource-Requirement";
    public static final String MODULE_ACTIVATOR = "Module-Activator";

    public enum Type {
        String,
        Version,
        VersionRange,
        Long,
        Double
    }

    static final Logger LOGGER = Logger.getLogger(ManifestBuilder.class.getPackage().getName());

    private final Map<String, String> identityRequirements = new LinkedHashMap<String, String>();
    private final List<String> genericCapabilities = new ArrayList<String>();
    private final List<String> genericRequirements = new ArrayList<String>();
    private List<String> lines = new ArrayList<String>();
    private Manifest manifest;

    public ManifestBuilder() {
        this("1.0");
    }

    public ManifestBuilder(String manifestVersion) {
        if (manifestVersion != null) {
            append(Attributes.Name.MANIFEST_VERSION + ": " + manifestVersion);
        }
    }

    public ManifestBuilder addIdentityCapability(String symbolicName, String version) {
        return addIdentityCapability(symbolicName, Version.parseVersion(version), null, null);
    }

    public ManifestBuilder addIdentityCapability(String symbolicName, Version version) {
        return addIdentityCapability(symbolicName, version, null, null);
    }

    public ManifestBuilder addIdentityCapability(String symbolicName, Version version, Map<String, String> atts, Map<String, String> dirs) {
        StringBuffer buffer = new StringBuffer(symbolicName);
        if (version != null) {
            buffer.append(";version=\"" + version + "\"");
        }
        if (atts != null) {
            for (Entry<String, String> entry : atts.entrySet()) {
                buffer.append(";" + entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
        }
        if (dirs != null) {
            for (Entry<String, String> entry : dirs.entrySet()) {
                buffer.append(";" + entry.getKey() + ":=\"" + entry.getValue() + "\"");
            }
        }
        addManifestHeader(RESOURCE_IDENTITY_CAPABILITY, buffer.toString());
        return this;
    }

    public ManifestBuilder addIdentityRequirement(String symbolicName, String version) {
        return addIdentityRequirement(symbolicName, new VersionRange(version), null, null);
    }

    public ManifestBuilder addIdentityRequirement(String symbolicName, VersionRange version) {
        return addIdentityRequirement(symbolicName, version, null, null);
    }

    public ManifestBuilder addIdentityRequirement(String symbolicName, VersionRange version, Map<String, String> atts, Map<String, String> dirs) {
        StringBuffer buffer = new StringBuffer(symbolicName);
        if (version != null) {
            buffer.append(";version=\"" + version + "\"");
        }
        if (atts != null) {
            for (Entry<String, String> entry : atts.entrySet()) {
                buffer.append(";" + entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
        }
        if (dirs != null) {
            for (Entry<String, String> entry : dirs.entrySet()) {
                buffer.append(";" + entry.getKey() + ":=\"" + entry.getValue() + "\"");
            }
        }
        addEntry(identityRequirements, buffer.toString());
        return this;
    }

    public ManifestBuilder addManifestHeader(String key, String value) {
        append(key + ": " + value);
        return this;
    }

    public ManifestBuilder addModuleActivator(String className) {
        addManifestHeader(MODULE_ACTIVATOR, className);
        return this;
    }

    public ManifestBuilder addModuleActivator(Class<?> clazz) {
        addManifestHeader(MODULE_ACTIVATOR, clazz.getName());
        return this;
    }

    public ManifestBuilder addGenericCapabilities(String... capabilities) {
        for (String entry : capabilities) {
            genericCapabilities.add(entry);
        }
        return this;
    }

    public ManifestBuilder addGenericCapability(String namespace, Map<String, String> atts, Map<String, String> dirs) {
        genericCapabilities.add(getCapabilitySpec(namespace, atts, dirs));
        return this;
    }

    public ManifestBuilder addGenericRequirements(String... capabilities) {
        for (String entry : capabilities) {
            genericRequirements.add(entry);
        }
        return this;
    }

    public ManifestBuilder addGenericRequirement(String namespace, Map<String, String> atts, Map<String, String> dirs) {
        genericRequirements.add(getCapabilitySpec(namespace, atts, dirs));
        return this;
    }

    private String getCapabilitySpec(String namespace, Map<String, String> atts, Map<String, String> dirs) {
        StringBuffer buffer = new StringBuffer(namespace);
        if (atts != null) {
            for (Entry<String, String> entry : atts.entrySet()) {
                buffer.append(";" + entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
        }
        if (dirs != null) {
            for (Entry<String, String> entry : dirs.entrySet()) {
                buffer.append(";" + entry.getKey() + ":=\"" + entry.getValue() + "\"");
            }
        }
        return buffer.toString();
    }

    // Strip attributes/directives to avoid duplicates
    private void addEntry(Map<String, String> target, String entry) {
        String key = entry;
        int index = entry.indexOf(";");
        if (index > 0) {
            key = entry.substring(0, index);
        }
        if (target.get(key) == null) {
            target.put(key, entry);
        } else {
            LOGGER.warnf("Ignore duplicate entery: %s", entry);
        }
    }

    /**
     * Validate a given manifest.
     *
     * @param manifest The given manifest
     * @return True if the manifest is valid
     */
    public static boolean isValidManifest(Manifest manifest) {
        if (manifest == null)
            return false;

        try {
            validateManifest(manifest);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Validate a given manifest.
     *
     * @param manifest The given manifest
     * @throws IllegalArgumentException if the given manifest is not a valid Gravia manifest
     */
    public static void validateManifest(Manifest manifest) {
        if (manifest == null)
            throw new IllegalArgumentException("Null manifest");

        String identitySpec = getManifestHeaderInternal(manifest, RESOURCE_IDENTITY_CAPABILITY);
        if (identitySpec == null)
            throw new IllegalArgumentException("Cannot obtain required header: " + RESOURCE_IDENTITY_CAPABILITY);
    }

    private static String getManifestHeaderInternal(Manifest manifest, String key) {
        Attributes attribs = manifest.getMainAttributes();
        String value = attribs.getValue(key);
        return value != null ? value.trim() : null;
    }

    public Manifest getManifest() {
        if (manifest == null) {
            addManifestHeader(RESOURCE_IDENTITY_REQUIREMENT, identityRequirements);
            addManifestHeader(RESOURCE_CAPABILITY, genericCapabilities);
            addManifestHeader(RESOURCE_REQUIREMENT, genericRequirements);
            StringWriter out = new StringWriter();
            PrintWriter pw = new PrintWriter(out);
            for(String line : lines) {
                byte[] bytes = line.getBytes();
                while (bytes.length >= 512) {
                    byte[] head = Arrays.copyOf(bytes, 256);
                    bytes = Arrays.copyOfRange(bytes, 256, bytes.length);
                    pw.println(new String(head));
                    pw.print(" ");
                }
                pw.println(new String(bytes));
            }

            String content = out.toString();
            if (LOGGER.isTraceEnabled())
                LOGGER.tracef(content);

            try {
                manifest = new Manifest(new ByteArrayInputStream(content.getBytes()));
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot create manifest", ex);
            }
        }
        return manifest;
    }

    private void addManifestHeader(String header, Map<String, String> source) {
        if (source.size() > 0) {
            int i = 0;
            StringBuffer buffer = new StringBuffer();
            for (String entry : source.values()) {
                buffer.append(i++ > 0 ? "," : "");
                buffer.append(entry);
            }
            addManifestHeader(header, buffer.toString());
        }
    }

    private void addManifestHeader(String header, List<String> source) {
        if (source.size() > 0) {
            int i = 0;
            StringBuffer buffer = new StringBuffer();
            for (String entry : source) {
                buffer.append(i++ > 0 ? "," : "");
                buffer.append(entry);
            }
            addManifestHeader(header, buffer.toString());
        }
    }

    public InputStream openStream() {
        Manifest manifest = getManifest();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            manifest.write(baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot provide manifest input stream", ex);
        }
    }

    public void append(String line) {
        if (manifest != null)
            throw new IllegalStateException("Cannot append to existing manifest");

        lines.add(line);
    }
}
