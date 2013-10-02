package org.jboss.gravia.runtime.embedded;

import org.jboss.gravia.runtime.PropertiesProvider;
import org.jboss.gravia.runtime.RuntimeFactory;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;

public final class EmbeddedRuntimeFactory implements RuntimeFactory {

    @Override
    public Runtime createRuntime(PropertiesProvider props) {
        return new EmbeddedRuntime(props);
    }
}