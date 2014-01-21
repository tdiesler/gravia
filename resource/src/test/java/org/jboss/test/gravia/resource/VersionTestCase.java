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


import org.jboss.gravia.resource.Version;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link Version}
 *
 * @author Thomas.Diesler@jboss.com
 */
public class VersionTestCase  {

    @Test
    public void testSnapshotVersion() throws Exception {
        Version version = Version.parseVersion("1.2.3.SNAPSHOT");
        Assert.assertEquals(1, version.getMajor());
        Assert.assertEquals(2, version.getMinor());
        Assert.assertEquals(3, version.getMicro());
        Assert.assertEquals("SNAPSHOT", version.getQualifier());

        version = Version.parseVersion("1.2.3-SNAPSHOT");
        Assert.assertEquals(1, version.getMajor());
        Assert.assertEquals(2, version.getMinor());
        Assert.assertEquals(3, version.getMicro());
        Assert.assertEquals("SNAPSHOT", version.getQualifier());
    }
}
