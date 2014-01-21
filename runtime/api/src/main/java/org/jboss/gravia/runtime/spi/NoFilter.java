package org.jboss.gravia.runtime.spi;
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

import java.util.Dictionary;
import java.util.Map;

import org.jboss.gravia.runtime.Filter;
import org.jboss.gravia.runtime.ServiceReference;

/**
 * A dummy filter that matches everything.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class NoFilter implements Filter {

    /** Singleton instance */
    public static final Filter INSTANCE = new NoFilter();

    private NoFilter() {
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean match(Dictionary dictionary) {
        return true;
    }

    @Override
    public boolean match(ServiceReference<?> reference) {
        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean matchCase(Dictionary dictionary) {
        return true;
    }

    @Override
    public boolean matches(Map<String, ?> map) {
        return true;
    }
}
