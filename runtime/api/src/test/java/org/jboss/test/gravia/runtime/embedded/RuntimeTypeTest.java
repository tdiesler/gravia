/*
 * #%L
 * JBossOSGi SPI
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.runtime.embedded;

import org.jboss.gravia.runtime.RuntimeType;
import org.junit.Assert;
import org.junit.Test;

/**
 * A simple {@link RuntimeType} test.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Sep-2013
 */
public class RuntimeTypeTest {

    @Test
    public void testRuntimeType() {
        Assert.assertSame(RuntimeType.KARAF, RuntimeType.getRuntimeType("Karaf"));
        Assert.assertSame(RuntimeType.KARAF, RuntimeType.getRuntimeType("karaf"));

        Assert.assertSame(RuntimeType.TOMCAT, RuntimeType.getRuntimeType("Tomcat"));
        Assert.assertSame(RuntimeType.TOMCAT, RuntimeType.getRuntimeType("tomcat"));

        Assert.assertSame(RuntimeType.WILDFLY, RuntimeType.getRuntimeType("WildFly"));
        Assert.assertSame(RuntimeType.WILDFLY, RuntimeType.getRuntimeType("wildfly"));

        Assert.assertSame(RuntimeType.OTHER, RuntimeType.getRuntimeType("Other"));
        Assert.assertSame(RuntimeType.OTHER, RuntimeType.getRuntimeType("foo"));
        Assert.assertSame(RuntimeType.OTHER, RuntimeType.getRuntimeType(""));
        Assert.assertSame(RuntimeType.OTHER, RuntimeType.getRuntimeType((String) null));
    }
}
