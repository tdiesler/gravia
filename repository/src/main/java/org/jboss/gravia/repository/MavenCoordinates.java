/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

package org.jboss.gravia.repository;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The artifact coordinates.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public final class MavenCoordinates {

    private final String groupId;
    private final String artifactId;
    private final String type;
    private final String version;
    private final String classifier;

    public static MavenCoordinates parse(String coordinates) {
        MavenCoordinates result;
        String[] parts = coordinates.split(":");
        if (parts.length == 3) {
            result = new MavenCoordinates(parts[0], parts[1], null, parts[2], null);
        } else if (parts.length == 4) {
            result = new MavenCoordinates(parts[0], parts[1], parts[2], parts[3], null);
        } else if (parts.length == 5) {
            result = new MavenCoordinates(parts[0], parts[1], parts[2], parts[3], parts[4]);
        } else {
            throw new IllegalArgumentException("Invalid coordinates: " + coordinates);
        }
        return result;
    }

    public static MavenCoordinates create(String groupId, String artifactId, String version, String type, String classifier) {
        return new MavenCoordinates(groupId, artifactId, type, version, classifier);
    }

    private MavenCoordinates(String groupId, String artifactId, String type, String version, String classifier) {
        if (groupId == null)
            throw new IllegalArgumentException("Null groupId");
        if (artifactId == null)
            throw new IllegalArgumentException("Null artifactId");
        if (version == null)
            throw new IllegalArgumentException("Null version");

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.type = (type != null ? type : "jar");
        this.version = version;
        this.classifier = classifier;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public String toExternalForm() {
        String clstr = classifier != null ? ":" + classifier : "";
        return groupId + ":" + artifactId + ":" + type + ":" + version + clstr;
    }

    public URL getArtifactURL(URL baseURL) {
        String base = baseURL.toExternalForm();
        if (base.endsWith("/") == false)
            base += "/";
        String urlstr = base + getArtifactPath();
        try {
            return new URL(urlstr);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid artifact URL: " + urlstr);
        }
    }

    public String getArtifactPath() {
        String dirstr = groupId.replace('.', '/') + "/" + artifactId + "/" + version;
        String clstr = classifier != null ? "-" + classifier : "";
        String path = dirstr + "/" + artifactId + "-" + version + clstr + "." + type;
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MavenCoordinates)) return false;
        MavenCoordinates other = (MavenCoordinates) obj;
        return toExternalForm().equals(other.toExternalForm());
    }

    @Override
    public int hashCode() {
        return toExternalForm().hashCode();
    }

    @Override
    public String toString() {
        return "MavenCoordinates[" + toExternalForm() + "]";
    }
}
