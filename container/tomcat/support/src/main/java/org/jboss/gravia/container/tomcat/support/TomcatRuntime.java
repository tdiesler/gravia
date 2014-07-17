/*
 * #%L
 * Gravia :: Container :: Tomcat :: Support
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
package org.jboss.gravia.container.tomcat.support;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.apache.catalina.UserDatabase;
import org.apache.catalina.users.MemoryUserDatabase;
import org.apache.naming.resources.DirContextURLStreamHandlerFactory;
import org.jboss.gravia.container.tomcat.WebAppContextListener;
import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.URLStreamHandlerTracker;
import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * The Tomcat {@link Runtime}
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class TomcatRuntime extends EmbeddedRuntime {

    public final static String TOMCAT_USER = "tomcat";

    private final UserDatabase userDatabase;

    public TomcatRuntime(PropertiesProvider propertiesProvider, Attachable context) {
        super(propertiesProvider, context);

        // Register the URLStreamHandler tracker
        URLStreamHandlerTracker tracker = new URLStreamHandlerTracker(getModuleContext());
        DirContextURLStreamHandlerFactory.addUserFactory(tracker);
        tracker.open();

        try {
            userDatabase = new MemoryUserDatabase();
            userDatabase.open();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot open user database", ex);
        }
        IllegalStateAssertion.assertNotNull(userDatabase.findUser(TOMCAT_USER), "Cannot obtain user: " + TOMCAT_USER);
    }

    @Override
    protected ModuleEntriesProvider getDefaultEntriesProvider(Module module, Attachable context) {
        ServletContext servletContext = context.getAttachment(WebAppContextListener.SERVLET_CONTEXT_KEY);
        return servletContext != null ? new ServletContextEntriesProvider(servletContext) : null;
    }

    public Path getCatalinaHome() {
        return Paths.get((String) getProperty("catalina.home"));
    }

    public UserDatabase getUserDatabase() {
        return userDatabase;
    }
}
