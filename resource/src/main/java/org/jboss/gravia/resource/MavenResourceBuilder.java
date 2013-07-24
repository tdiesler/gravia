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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link Resource} builder that understands {@link MavenCoordinates}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class MavenResourceBuilder extends DefaultResourceBuilder {

    public Capability addIdentityCapability(MavenCoordinates mavenid) {
        Capability icap = addIdentityCapability(getSymbolicName(mavenid), getVersion(mavenid), null, null);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE, mavenid);
        return icap;
    }

    public static String getSymbolicName(MavenCoordinates mavenid) {
        return mavenid.getGroupId() + "." + mavenid.getArtifactId();
    }

    public static Version getVersion(MavenCoordinates mavenid) {
        return Version.parseVersion(cleanupVersion(mavenid.getVersion()));
    }

    /**
     * Clean up version parameters. Other builders use more fuzzy definitions of
     * the version syntax. This method cleans up such a version to match an OSGi
     * version.
     * 
     * https://github.com/apache/felix/blob/trunk/bundleplugin/src/main/java/org/apache/maven/shared/osgi/DefaultMaven2OsgiConverter.java
     * https://github.com/apache/felix/commit/39cc9ac8cbb3e5d8a76793b81b7cb9b04bca85b0
     *
     */
    private static final Pattern FUZZY_VERSION = Pattern.compile("(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([^a-zA-Z0-9](.*))?", Pattern.DOTALL);

    private static String cleanupVersion(String version) {
        StringBuffer result = new StringBuffer();
        Matcher m = FUZZY_VERSION.matcher(version);
        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(3);
            String micro = m.group(5);
            String qualifier = m.group(7);

            if (major != null) {
                result.append(major);
                if (minor != null) {
                    result.append(".");
                    result.append(minor);
                    if (micro != null) {
                        result.append(".");
                        result.append(micro);
                        if (qualifier != null) {
                            result.append(".");
                            cleanupModifier(result, qualifier);
                        }
                    } else if (qualifier != null) {
                        result.append(".0.");
                        cleanupModifier(result, qualifier);
                    } else {
                        result.append(".0");
                    }
                } else if (qualifier != null) {
                    result.append(".0.0.");
                    cleanupModifier(result, qualifier);
                } else {
                    result.append(".0.0");
                }
            }
        } else {
            result.append("0.0.0.");
            cleanupModifier(result, version);
        }
        return result.toString();
    }

    private static void cleanupModifier(StringBuffer result, String modifier) {
        for (int i = 0; i < modifier.length(); i++) {
            char c = modifier.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-')
                result.append(c);
            else
                result.append('_');
        }
    }
}
