/*
 * #%L
 * Gravia :: Arquillian :: Container
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
package org.jboss.gravia.arquillian.container.managed;

import java.util.Map;

import javax.management.MBeanServerConnection;

import org.jboss.gravia.arquillian.container.SetupTask;

/**
 * A task which is run for container setup.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Dec-2013
 */
public abstract class ManagedSetupTask extends SetupTask {

    public interface ManagedContext extends SetupContext {

        MBeanServerConnection getMBeanServer();

        Map<String, String> getConfiguration();
    }

    protected void beforeDeploy(ManagedContext context) throws Exception {
        // do nothing
    }

    protected void beforeStop(ManagedContext context) throws Exception {
        // do nothing
    }
}
