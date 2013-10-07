package org.jboss.gravia.runtime.embedded;

import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeFactory;

public final class EmbeddedRuntimeFactory implements RuntimeFactory {

    @Override
    public Runtime createRuntime(PropertiesProvider props) {
        return new EmbeddedRuntime(props);
    }
}