/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.gravia.runtime;

import java.util.concurrent.TimeUnit;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.gravia.runtime.sub.ApplicationActivator;
import org.jboss.test.gravia.runtime.sub.SimpleServlet;
import org.jboss.test.support.HttpRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test webapp deployemnts
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Oct-2013
 */
public abstract class BasicPortableTest {

    public static WebArchive deployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "simple.war");
        archive.addClasses(HttpRequest.class, ApplicationActivator.class, SimpleServlet.class);
        return archive;
    }

    @Test
    public void testWarDeployment() throws Exception {
        String result = performCall("/simple/servlet?input=Hello");
        Assert.assertEquals("Hello from Module[simple.war:1.0.0]", result);
    }

    private String performCall(String path) throws Exception {
        String urlspec = "http://localhost:8080" + path;
        return HttpRequest.get(urlspec, 10, TimeUnit.SECONDS);
    }
}
