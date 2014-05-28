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
package org.jboss.test.gravia.runtime.embedded;

import org.jboss.gravia.runtime.RuntimeType;
import org.junit.Assert;
import org.junit.Test;

/**
 * A simple {@link RuntimeType} test.
 *
 * @author thomas.diesler@jboss.com
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

    @Test
    public void testRuntimeRelevant() {

        Assert.assertTrue(RuntimeType.isRuntimeRelevant(RuntimeType.KARAF, null, null));
        Assert.assertTrue(RuntimeType.isRuntimeRelevant(RuntimeType.KARAF, "karaf", null));
        Assert.assertFalse(RuntimeType.isRuntimeRelevant(RuntimeType.KARAF, "karaf", "karaf"));
        Assert.assertFalse(RuntimeType.isRuntimeRelevant(RuntimeType.KARAF, null, "karaf"));

        Assert.assertTrue(RuntimeType.isRuntimeRelevant(RuntimeType.KARAF, "karaf,foo", null));
        Assert.assertTrue(RuntimeType.isRuntimeRelevant(RuntimeType.KARAF, "foo,karaf", null));
        Assert.assertTrue(RuntimeType.isRuntimeRelevant(RuntimeType.KARAF, "foo, karaf", null));
    }
}
