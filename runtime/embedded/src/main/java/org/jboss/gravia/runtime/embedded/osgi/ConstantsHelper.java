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
package org.jboss.gravia.runtime.embedded.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;

/**
 * String representation for common OSGi Constants
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public abstract class ConstantsHelper {

    /**
     * Return the string representation of a {@link Bundle} state
     */
    public static String bundleState(int bundleState) {
        String retState = "[" + bundleState + "]";
        if (Bundle.UNINSTALLED == bundleState)
            retState = "UNINSTALLED";
        else if (Bundle.INSTALLED == bundleState)
            retState = "INSTALLED";
        else if (Bundle.RESOLVED == bundleState)
            retState = "RESOLVED";
        else if (Bundle.STARTING == bundleState)
            retState = "STARTING";
        else if (Bundle.STOPPING == bundleState)
            retState = "STOPPING";
        else if (Bundle.ACTIVE == bundleState)
            retState = "ACTIVE";
        return retState;
    }

    /**
     * Return the string representation of a {@link BundleEvent} type
     */
    public static String bundleEvent(int eventType) {
        String retType = "[" + eventType + "]";
        if (BundleEvent.INSTALLED == eventType)
            retType = "INSTALLED";
        else if (BundleEvent.LAZY_ACTIVATION == eventType)
            retType = "LAZY_ACTIVATION";
        else if (BundleEvent.RESOLVED == eventType)
            retType = "RESOLVED";
        else if (BundleEvent.STARTING == eventType)
            retType = "STARTING";
        else if (BundleEvent.STARTED == eventType)
            retType = "STARTED";
        else if (BundleEvent.STOPPING == eventType)
            retType = "STOPPING";
        else if (BundleEvent.STOPPED == eventType)
            retType = "STOPPED";
        else if (BundleEvent.UNINSTALLED == eventType)
            retType = "UNINSTALLED";
        else if (BundleEvent.UNRESOLVED == eventType)
            retType = "UNRESOLVED";
        else if (BundleEvent.UPDATED == eventType)
            retType = "UPDATED";
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

    public static String frameworkEvent(int eventType) {
        String retType = "[" + eventType + "]";
        if (FrameworkEvent.ERROR == eventType)
            retType = "ERROR";
        else if (FrameworkEvent.INFO == eventType)
            retType = "INFO";
        else if (FrameworkEvent.PACKAGES_REFRESHED == eventType)
            retType = "PACKAGES_REFRESHED";
        else if (FrameworkEvent.STARTED == eventType)
            retType = "STARTED";
        else if (FrameworkEvent.STARTLEVEL_CHANGED == eventType)
            retType = "STARTLEVEL_CHANGED";
        else if (FrameworkEvent.STOPPED == eventType)
            retType = "STOPPED";
        else if (FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED == eventType)
            retType = "STOPPED_BOOTCLASSPATH_MODIFIED";
        else if (FrameworkEvent.STOPPED_UPDATE == eventType)
            retType = "STOPPED_UPDATE";
        else if (FrameworkEvent.WAIT_TIMEDOUT == eventType)
            retType = "WAIT_TIMEDOUT";
        else if (FrameworkEvent.WARNING == eventType)
            retType = "WARNING";
        return retType;
    }

    /**
     * Return the string representation of a LogService level
     */
    public static String logLevel(int level) {
        String logLevel = "[" + level + "]";
        switch (level) {
            // LogService.LOG_DEBUG:
            case 0x4:
                logLevel = "DEBUG";
                break;
                // LogService.LOG_INFO:
            case 0x3:
                logLevel = "INFO";
                break;
                // LogService.LOG_WARNING:
            case 0x2:
                logLevel = "WARN";
                break;
                // LogService.LOG_ERROR
            case 0x1:
                logLevel = "ERROR";
                break;
        }
        return logLevel;
    }
}
