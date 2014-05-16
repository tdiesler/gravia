/*
 * #%L
 * Gravia :: Runtime :: API
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
package org.jboss.gravia.runtime.spi;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * A proxy for the URLStreamHandlerTracker
 *
 * @author thomas.diesler@jboss.com
 * @since 16-May-2014
 *
 * @ThreadSafe
 */
public class URLStreamHandlerFactoryProxy implements URLStreamHandlerFactory {

    private static URLStreamHandlerFactory INSTANCE = new URLStreamHandlerFactoryProxy();
    private static AtomicBoolean registered = new AtomicBoolean();
    private static URLStreamHandlerTracker delegate;

    public static URLStreamHandlerFactory getInstance() {
        return INSTANCE;
    }

    public static void setDelegate(URLStreamHandlerTracker tracker) {
        IllegalArgumentAssertion.assertNotNull(tracker, "tracker");
        synchronized (URLStreamHandlerFactoryProxy.class) {
            if (delegate != null) {
                delegate.close();
            }
            delegate = tracker;
            delegate.open();
        }
    }

    public static boolean register() {
        synchronized (URLStreamHandlerFactoryProxy.class) {
            IllegalStateAssertion.assertNotNull(delegate, "Cannot register without delegate");
            if (registered.compareAndSet(false, true)) {
                try {
                    URL.setURLStreamHandlerFactory(INSTANCE);
                    return true;
                } catch (Error er) {
                    // ignore
                }
            }
        }
        return false;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        synchronized (URLStreamHandlerFactoryProxy.class) {
            IllegalStateAssertion.assertNotNull(delegate, "Cannot create URLStreamHandler without delegate");
            return delegate.createURLStreamHandler(protocol);
        }
    }
}
