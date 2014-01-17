/*
 * #%L
 * JBossOSGi Resolver API
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
