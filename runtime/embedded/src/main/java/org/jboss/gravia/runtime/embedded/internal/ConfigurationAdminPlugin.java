/*
 * #%L
 * JBossOSGi Framework
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
package org.jboss.gravia.runtime.embedded.internal;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;

import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.embedded.spi.AbstractRuntimePlugin;
import org.jboss.gravia.utils.StringPropertyReplacer;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * The internal ConfigurationAdmin plugin.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class ConfigurationAdminPlugin extends AbstractRuntimePlugin {

    @Override
    public String getBundleActivator() {
        return "org.apache.felix.cm.impl.ConfigurationManager";
    }

    @Override
    public void start(ModuleContext context) throws Exception {
        super.start(context);

        ServiceReference<ConfigurationAdmin> sref = context.getServiceReference(ConfigurationAdmin.class);
        ConfigurationAdmin configAdmin = context.getService(sref);

        Runtime runtime = context.getModule().adapt(Runtime.class);
        String confspecs = (String) runtime.getProperty(Constants.RUNTIME_CONFIGURATIONS);
        if (confspecs != null) {
            for (String spec : confspecs.split(",")) {
                String urlspec = StringPropertyReplacer.replaceProperties(spec.trim());
                URL specurl = new URL(urlspec);
                String path = specurl.getPath();
                String pid = path.substring(path.lastIndexOf('/') + 1);
                if (pid.endsWith(".cfg")) {
                    pid = pid.substring(0, pid.length() - 4);
                }

                Properties props = new Properties();
                props.load(specurl.openStream());

                Configuration config = configAdmin.getConfiguration(pid, null);
                config.update(propsToMap(props));
            }
        }
    }

    private Dictionary<String, ?> propsToMap(Properties props) {
        Dictionary<String, Object> result = new Hashtable<String, Object>();
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            result.put(key, value);
        }
        return result;
    }
}
