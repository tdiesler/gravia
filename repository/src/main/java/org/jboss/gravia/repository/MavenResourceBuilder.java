/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
package org.jboss.gravia.repository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.Version;

/**
 * The {@link Resource} builder that understands {@link MavenCoordinates}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class MavenResourceBuilder extends DefaultRepositoryResourceBuilder {

    public Capability addIdentityCapability(MavenCoordinates mavenid) {
        Capability icap = addIdentityCapability(getSymbolicName(mavenid), getVersion(mavenid), null, null);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE, mavenid.toExternalForm());
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
