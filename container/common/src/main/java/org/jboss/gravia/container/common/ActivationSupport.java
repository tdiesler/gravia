/*
 * #%L
 * Gravia :: Container :: Tomcat :: Extension
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
package org.jboss.gravia.container.common;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.utils.NotNullException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * The runtime activation support
 *
 * @author Thomas.Diesler@jboss.com
 * @since 17-Feb-2014
 */
public final class ActivationSupport {

    // Hide ctor
    private ActivationSupport() {

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void initConfigurationAdmin(File configsDir) {
        NotNullException.assertValue(configsDir, "configsDir");
        ConfigurationAdmin configAdmin = getConfigurationAdmin();
        if (!configsDir.isDirectory() || configAdmin == null) {
            LOGGER.info("No ConfigurationAdmin content");
            return;
        }
        LOGGER.info("Loading ConfigurationAdmin content from: " + configsDir);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".cfg");
            }
        };
        for (String name : configsDir.list(filter)) {
            LOGGER.info("Loading configuration: " + name);
            String pid = name.substring(0, name.length() - 4);
            try {
                FileInputStream fis = new FileInputStream(new File(configsDir, name));
                Properties props = new Properties();
                props.load(fis);
                fis.close();
                if (!props.isEmpty()) {
                    Configuration config = configAdmin.getConfiguration(pid, null);
                    config.update((Hashtable) props);
                }
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private static ConfigurationAdmin getConfigurationAdmin() {
        ModuleContext syscontext = RuntimeLocator.getRequiredRuntime().getModuleContext();
        ServiceReference<ConfigurationAdmin> sref = syscontext.getServiceReference(ConfigurationAdmin.class);
        return sref != null ? syscontext.getService(sref) : null;
    }
}
