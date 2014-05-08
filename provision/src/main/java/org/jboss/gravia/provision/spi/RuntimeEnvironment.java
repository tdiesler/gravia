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
package org.jboss.gravia.provision.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.gravia.Constants;
import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.spi.AbstractEnvironment;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultMatchPolicy;
import org.jboss.gravia.resource.DefaultResourceStore;
import org.jboss.gravia.resource.MatchPolicy;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.ResourceStore;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.SynchronousModuleListener;
import org.jboss.gravia.utils.IOUtils;
import org.jboss.gravia.utils.NotNullException;

/**
 * An {@link Environment} that maintains the set of runtime resources.
 *
 * @author thomas.diesler@jboss.com
 * @since 07-Jan-2014
 */
public class RuntimeEnvironment extends AbstractEnvironment {

    private final ResourceStore systemStore;
    private final Runtime runtime;

    public RuntimeEnvironment(Runtime runtime) {
        this(runtime, new DefaultResourceStore("SystemResources"), new DefaultMatchPolicy());
    }

    public RuntimeEnvironment(Runtime runtime, ResourceStore systemStore, MatchPolicy matchPolicy) {
        super(RuntimeEnvironment.class.getSimpleName(), matchPolicy);
        NotNullException.assertValue(runtime, "runtime");
        NotNullException.assertValue(systemStore, "systemStore");
        NotNullException.assertValue(matchPolicy, "matchPolicy");
        this.systemStore = systemStore;
        this.runtime = runtime;

        // Add the initial set of modules
        for (Module module : runtime.getModules()) {
            addRuntimeResource(module.adapt(Resource.class));
        }

        // Track installed/uninstalled modules
        ModuleListener listener = new SynchronousModuleListener() {
            @Override
            public void moduleChanged(ModuleEvent event) {
                Module module = event.getModule();
                if (event.getType() == ModuleEvent.INSTALLED) {
                    addRuntimeResource(module.adapt(Resource.class));
                } else if (event.getType() == ModuleEvent.UNINSTALLED) {
                    removeRuntimeResource(module.getIdentity());
                }
            }
        };
        ModuleContext syscontext = runtime.getModuleContext();
        syscontext.addModuleListener(listener);
    }

    public static RuntimeEnvironment assertRuntimeEnvironment(Environment env) {
        if (!(env instanceof RuntimeEnvironment))
            throw new IllegalArgumentException("Not an RuntimeEnvironment: " + env);
        return (RuntimeEnvironment) env;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public Iterator<Resource> getRuntimeResources() {
        return super.getResources();
    }

    public Resource addRuntimeResource(Resource resource) {
        return super.addResource(resource);
    }

    public Resource removeRuntimeResource(ResourceIdentity identity) {
        return super.removeResource(identity);
    }

    public ResourceStore getSystemStore() {
        return systemStore;
    }

    public RuntimeEnvironment initDefaultContent() {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        File repositoryDir = new File((String) runtime.getProperty(Constants.PROPERTY_REPOSITORY_STORAGE_DIR));
        File environmentXML = new File(repositoryDir, "environment.xml");
        try {
            InputStream content = new FileInputStream(environmentXML);
            initDefaultContent(content);
        } catch (FileNotFoundException ex) {
            // ignore
        }
        return this;
    }

    public RuntimeEnvironment initDefaultContent(InputStream content) {
        NotNullException.assertValue(content, "content");
        try {
            RepositoryReader reader = new DefaultRepositoryXMLReader(content);
            Resource xmlres = reader.nextResource();
            while (xmlres != null) {
                systemStore.addResource(xmlres);
                xmlres = reader.nextResource();
            }
        } finally {
            IOUtils.safeClose(content);
        }
        return this;
    }

    @Override
    public Resource getResource(ResourceIdentity identity) {
        Resource resource = super.getResource(identity);
        if (resource == null) {
            resource = systemStore.getResource(identity);
        }
        return resource;
    }

    @Override
    public Set<Capability> findProviders(Requirement requirement) {
        Set<Capability> result = new LinkedHashSet<Capability>();
        result.addAll(super.findProviders(requirement));
        result.addAll(systemStore.findProviders(requirement));
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Environment cloneEnvironment() {
        return new ClonedRuntimeEnvironment(this);
    }

    @Override
    public Iterator<Resource> getResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource addResource(Resource resource) {
        return systemStore.addResource(resource);
    }

    @Override
    public Resource removeResource(ResourceIdentity identity) {
        return systemStore.removeResource(identity);
    }

    static class ClonedRuntimeEnvironment extends AbstractEnvironment {

        private final ResourceStore systemStore;

        ClonedRuntimeEnvironment(RuntimeEnvironment environment) {
            super(ClonedRuntimeEnvironment.class.getSimpleName(), environment.getMatchPolicy());
            this.systemStore = environment.getSystemStore();
            Iterator<Resource> itres = environment.getRuntimeResources();
            while (itres.hasNext()) {
                addResource(itres.next());
            }
        }

        @Override
        public Environment cloneEnvironment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource getResource(ResourceIdentity identity) {
            Resource resource = super.getResource(identity);
            if (resource == null) {
                resource = systemStore.getResource(identity);
            }
            return resource;
        }

        @Override
        public Set<Capability> findProviders(Requirement requirement) {
            Set<Capability> result = new LinkedHashSet<Capability>();
            result.addAll(super.findProviders(requirement));
            result.addAll(systemStore.findProviders(requirement));
            return Collections.unmodifiableSet(result);
        }
    }
}
