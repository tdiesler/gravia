/*
 * #%L
 * Gravia :: Provision
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
package org.jboss.gravia.provision;

/**
 * A Provision exception
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public final class ProvisionException extends Exception {

    private static final long serialVersionUID = -7127486814369591376L;

    public ProvisionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProvisionException(String message) {
        super(message);
    }

    public ProvisionException(Throwable cause) {
        super(cause);
    }
}