package org.jboss.gravia.runtime.osgi;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.osgi.framework.Bundle;

public interface OSGiRuntime extends Runtime {

    Module installModule(Bundle bundle) throws ModuleException;
}