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
package org.jboss.test.gravia.resource;


import org.jboss.gravia.resource.MavenCoordinates;
import org.junit.Assert;
import org.junit.Test;

public class MavenCoordinatesTest {

    static String GROUP_ID = "groupId";
    static String ARTIFACT_ID = "artifactId";
    static String VERSION = "version";
    static String TYPE = "type";
    static String CLASSIFIER = "classifier";

    static String DEFAULT_TYPE = "jar";

    static String SIMPLE_COORDS = GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION;
    static String COORDS_WITH_TYPE = GROUP_ID + ":" + ARTIFACT_ID + ":" + TYPE + ":" + VERSION;
    static String COORDS_WITH_TYPE_AND_CLASSIFIER = GROUP_ID + ":" + ARTIFACT_ID + ":" + TYPE + ":" + CLASSIFIER + ":" + VERSION;


    @Test
    public void testParse() {
        MavenCoordinates coords = MavenCoordinates.parse(SIMPLE_COORDS);
        assertCoords(coords, GROUP_ID, ARTIFACT_ID, VERSION, DEFAULT_TYPE, null);

        coords = MavenCoordinates.parse(COORDS_WITH_TYPE);
        assertCoords(coords, GROUP_ID, ARTIFACT_ID, VERSION, TYPE, null);

        coords = MavenCoordinates.parse(COORDS_WITH_TYPE_AND_CLASSIFIER);
        assertCoords(coords, GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalCoords() {
        MavenCoordinates.parse(GROUP_ID + ":" + ARTIFACT_ID);
    }

    private static void assertCoords(MavenCoordinates coordinates, String groupId, String artifactId, String version, String type, String classifier) {
        Assert.assertEquals(groupId, coordinates.getGroupId());
        Assert.assertEquals(artifactId, coordinates.getArtifactId());
        Assert.assertEquals(version, coordinates.getVersion());
        Assert.assertEquals(type, coordinates.getType());
        Assert.assertEquals(classifier, coordinates.getClassifier());
    }
}
