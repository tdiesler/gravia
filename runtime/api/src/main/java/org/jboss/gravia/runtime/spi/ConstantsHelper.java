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

import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ServiceEvent;


/**
 * An internal string representation for common constants
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
final class ConstantsHelper {

    // hide ctor
    private ConstantsHelper() {
    }

    /**
     * Return the string representation of a {@link ModuleEvent} type
     */
    public static String moduleEvent(int eventType) {
        String retType = "[" + eventType + "]";
        if (ModuleEvent.INSTALLED == eventType)
            retType = "INSTALLED";
        else if (ModuleEvent.RESOLVED == eventType)
            retType = "RESOLVED";
        else if (ModuleEvent.STARTING == eventType)
            retType = "STARTING";
        else if (ModuleEvent.STARTED == eventType)
            retType = "STARTED";
        else if (ModuleEvent.STOPPING == eventType)
            retType = "STOPPING";
        else if (ModuleEvent.STOPPED == eventType)
            retType = "STOPPED";
        else if (ModuleEvent.UNINSTALLED == eventType)
            retType = "UNINSTALLED";
        return retType;
    }

    /**
     * Return the string representation of a {@link ServiceEvent} type
     */
    public static String serviceEvent(int eventType) {
        String retType = "[" + eventType + "]";
        if (ServiceEvent.REGISTERED == eventType)
            retType = "REGISTERED";
        else if (ServiceEvent.UNREGISTERING == eventType)
            retType = "UNREGISTERING";
        else if (ServiceEvent.MODIFIED == eventType)
            retType = "MODIFIED";
        else if (ServiceEvent.MODIFIED_ENDMATCH == eventType)
            retType = "MODIFIED_ENDMATCH";
        return retType;
    }
}
