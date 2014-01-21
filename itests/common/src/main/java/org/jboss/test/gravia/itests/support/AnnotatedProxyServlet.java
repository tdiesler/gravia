/*
 * #%L
 * Gravia :: Integration Tests :: Common
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
package org.jboss.test.gravia.itests.support;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.felix.http.proxy.ProxyServlet;
import org.osgi.service.http.HttpService;

/**
 * Proxy servlet for the {@link HttpService}
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
@SuppressWarnings("serial")
@WebServlet(name = "HttpServiceServlet", urlPatterns = { "/*" }, loadOnStartup = 1)
public class AnnotatedProxyServlet extends HttpServlet
{
    private final ProxyServlet delegate = new ProxyServlet();

    public void init(ServletConfig config) throws ServletException {
        delegate.init(config);
    }

    public void destroy() {
        delegate.destroy();
    }

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        delegate.service(req, res);
    }
}
