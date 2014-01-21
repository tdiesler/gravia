/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
package org.jboss.gravia.runtime.embedded.internal;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;

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
        String confspecs = (String) runtime.getProperty(org.jboss.gravia.Constants.RUNTIME_CONFIGURATIONS);
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
