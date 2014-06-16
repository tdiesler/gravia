/*
 * #%L
 * Gravia :: Container :: Tomcat :: Webapp
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
package org.jboss.gravia.container.tomcat.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceTracker;
import org.jboss.gravia.runtime.spi.RuntimeLogger;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

/**
 * Register Gravia system services.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Nov-2013
 */
public class GraviaHttpServiceActivator implements ServletContextListener {

    private ServiceTracker<HttpService, HttpService> tracker;
    private static final String SYSTEM_ALIAS = "/system";

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();
        BundleContext bundleContext = (BundleContext) servletContext.getAttribute(BundleContext.class.getName());
        final Module module = bundleContext.getBundle().adapt(Module.class);
        final ModuleContext context = module.getModuleContext();

        tracker = new ServiceTracker<HttpService, HttpService>(context, HttpService.class, null) {

            @Override
            public HttpService addingService(ServiceReference<HttpService> sref) {
                HttpService service = super.addingService(sref);
                try {
                    RuntimeLogger.LOGGER.info("Register system HttpService with alias: " + SYSTEM_ALIAS);
                    service.registerServlet(SYSTEM_ALIAS, new HttpServiceServlet(module), null, null);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
                return service;
            }

            @Override
            public void removedService(ServiceReference<HttpService> reference, HttpService service) {
                RuntimeLogger.LOGGER.info("Unregister system HttpService with alias: " + SYSTEM_ALIAS);
                service.unregister(SYSTEM_ALIAS);
                super.removedService(reference, service);
            }
        };
        tracker.open();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (tracker != null) {
            tracker.close();
        }
    }

    @SuppressWarnings("serial")
    static final class HttpServiceServlet extends HttpServlet {

        private final Module module;

        // This hides the default ctor and verifies that this instance is used
        HttpServiceServlet(Module module) {
            this.module = module;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
            PrintWriter out = res.getWriter();
            out.print(module.getIdentity().getSymbolicName());
            out.close();
        }
    }
}
