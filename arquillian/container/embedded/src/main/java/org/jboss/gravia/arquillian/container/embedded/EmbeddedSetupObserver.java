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
package org.jboss.gravia.arquillian.container.embedded;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.core.spi.context.ObjectStore;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.gravia.arquillian.container.SetupObserver;
import org.jboss.gravia.arquillian.container.SetupTask;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.utils.IllegalStateAssertion;
import org.junit.Assert;

/**
 * An Arquillian container setup observer.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 18-Jun-2014
 */
public class EmbeddedSetupObserver extends SetupObserver<SetupTask> {

    @Override
    public void handleBeforeSuite(BeforeSuite event) throws Throwable {
        super.handleBeforeSuite(event);

        Runtime runtime = RuntimeLocator.getRuntime();
        IllegalStateAssertion.assertNull(runtime, "Embedded Runtime already created without @RunWith(Arquillian.class)");

        runtime = EmbeddedUtils.getEmbeddedRuntime();
        ObjectStore suiteStore = getSuiteObjectStore();
        suiteStore.add(Runtime.class, runtime);

        // Do additional setup
        Iterator<EmbeddedRuntimeSetup> itsetup = ServiceLoader.load(EmbeddedRuntimeSetup.class).iterator();
        while(itsetup.hasNext()) {
            EmbeddedRuntimeSetup setup = itsetup.next();
            setup.setupEmbeddedRuntime(suiteStore);
        }
    }

    @Override
    public void handleAfterSuite(AfterSuite event) throws Throwable {
        Runtime runtime = getSuiteObjectStore().get(Runtime.class);
        if (runtime != null && !runtime.shutdownComplete()) {
            Assert.assertTrue(runtime.shutdown().awaitShutdown(20, TimeUnit.SECONDS));
            RuntimeLocator.releaseRuntime();
        }
        super.handleAfterSuite(event);
    }
}
