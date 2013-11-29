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
package org.jboss.gravia.runtime.embedded.spi;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.felix.http.proxy.ProxyServlet;

/**
 * Proxy servlet for the {@link HttpService}
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
@SuppressWarnings("serial")
@WebServlet(name = "HttpServiceServlet", urlPatterns = { "/*" }, loadOnStartup = 1)
public class HttpServiceProxyServlet extends HttpServlet
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
