package org.jboss.gravia.runtime.osgi;

import org.jboss.gravia.runtime.PropertiesProvider;
import org.jboss.gravia.runtime.RuntimeFactory;
import org.jboss.gravia.runtime.osgi.internal.OSGiRuntimeImpl;

public final class OSGiRuntimeFactory implements RuntimeFactory {

    @Override
    public OSGiRuntime createRuntime(PropertiesProvider props) {
        return new OSGiRuntimeImpl(props);
    }
}