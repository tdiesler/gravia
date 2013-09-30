/*
 * #%L
 * JBossOSGi SPI
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
package org.jboss.gravia.runtime.spi;

import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ServiceEvent;


/**
 * String representation for common constants
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public final class ConstantsHelper {

    // hide ctor
    private ConstantsHelper() {
    }

    /**
     * Return the string representation of a {@link ModuleEvent} type
     */
    public static String bundleEvent(int eventType) {
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
        else if (ModuleEvent.UNRESOLVED == eventType)
            retType = "UNRESOLVED";
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
