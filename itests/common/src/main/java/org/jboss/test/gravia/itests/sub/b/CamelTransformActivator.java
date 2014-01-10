/*
 * #%L
 * Gravia :: Integration Tests :: Common
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
package org.jboss.test.gravia.itests.sub.b;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.gravia.Constants;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.osgi.service.http.HttpService;

public class CamelTransformActivator implements ModuleActivator {

    private HttpService httpService;
    private CamelContext camelctx;

    @Override
    public void start(final ModuleContext context) throws Exception {

        camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").transform(body().prepend("Hello "));
            }
        });
        camelctx.start();

        // [TODO] replace with ServiceTracker
        ServiceReference<HttpService> sref = context.getServiceReference(HttpService.class);
        if (sref != null) {
            registerHttpService(context, sref);
        } else {
            ServiceListener listener = new ServiceListener() {
                @Override
                public void serviceChanged(ServiceEvent event) {
                    if (ServiceEvent.REGISTERED == event.getType()) {
                        ServiceReference<?> sref = event.getServiceReference();
                        String[] classes = (String[]) sref.getProperty(Constants.OBJECTCLASS);
                        if (Arrays.asList(classes).contains(HttpService.class.getName())) {
                            registerHttpService(context, sref);
                        }
                    }
                }
            };
            context.addServiceListener(listener);
        }
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        camelctx.stop();
        if (httpService != null) {
            httpService.unregister("/service");
        }
    }

    private void registerHttpService(final ModuleContext context, final ServiceReference<?> sref) {
        try {
            httpService = (HttpService) context.getService(sref);
            httpService.registerServlet("/service", new HttpServiceServlet(camelctx), null, null);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot register HttpService", ex);
        }
    }

    @SuppressWarnings("serial")
    static final class HttpServiceServlet extends HttpServlet {

        private final CamelContext camelctx;

        HttpServiceServlet(CamelContext camelctx) {
            this.camelctx = camelctx;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
            PrintWriter out = res.getWriter();
            String msg = req.getParameter("test");
            ProducerTemplate producer = camelctx.createProducerTemplate();
            String result = producer.requestBody("direct:start", msg, String.class);
            out.print(result);
            out.close();
        }
    }
}