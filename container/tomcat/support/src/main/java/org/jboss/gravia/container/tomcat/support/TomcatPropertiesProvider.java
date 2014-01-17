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
package org.jboss.gravia.container.tomcat.support;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.jboss.gravia.Constants;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
import org.jboss.gravia.utils.NotNullException;

/**
 * The Tomcat {@link PropertiesProvider}
 *
 * @author thomas.diesler@jboss.com
 * @since 17-Jan-2014
 */
public class TomcatPropertiesProvider implements PropertiesProvider {

    private final static File catalinaHome = new File(SecurityActions.getSystemProperty("catalina.home", null));
    private final static File catalinaWork = new File(catalinaHome.getPath() + File.separator + "work");

    private final ServletContext servletContext;
    private PropertiesProvider delegate;

    public TomcatPropertiesProvider(ServletContext servletContext) {
        NotNullException.assertValue(servletContext, "servletContext");
        this.servletContext = servletContext;
    }

    public File getCatalinaHome() {
        return catalinaHome;
    }

    public File getCatalinaWork() {
        return catalinaWork;
    }
    public Object getProperty(String key) {
        return getInternalPropertiesProvider().getProperty(key);
    }

    public Object getProperty(String key, Object defaultValue) {
        return getInternalPropertiesProvider().getProperty(key, defaultValue);
    }

    private synchronized PropertiesProvider getInternalPropertiesProvider() {
        if (delegate == null) {
            Properties properties = initialProperties(servletContext);
            delegate = new DefaultPropertiesProvider(properties, true);
        }
        return delegate;
    }

    protected Properties initialProperties(ServletContext servletContext) {

        Properties properties = new Properties();
        properties.setProperty(Constants.RUNTIME_TYPE, "tomcat");

        String storageClean = servletContext.getInitParameter(Constants.RUNTIME_STORAGE_CLEAN);
        if (storageClean == null) {
            storageClean = Constants.RUNTIME_STORAGE_CLEAN_ONFIRSTINIT;
        }
        properties.setProperty(Constants.RUNTIME_STORAGE_CLEAN, storageClean);

        String storageDir = servletContext.getInitParameter(Constants.RUNTIME_STORAGE);
        if (storageDir == null) {
            storageDir = new File(catalinaWork.getPath() + File.separator + Constants.RUNTIME_STORAGE_DEFAULT).getAbsolutePath();
        }
        properties.setProperty(Constants.RUNTIME_STORAGE, storageDir);

        storageDir = servletContext.getInitParameter(Constants.PROPERTY_REPOSITORY_STORAGE_DIR);
        if (storageDir == null) {
            storageDir = new File(catalinaWork.getPath() + File.separator + "repository").getAbsolutePath();
        }
        properties.setProperty(Constants.PROPERTY_REPOSITORY_STORAGE_DIR, storageDir);

        return properties;
    }
}
