/*
 * #%L
 * Gravia :: Integration Tests :: Common
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.test.gravia.itests.support;

import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.ManifestContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.container.WebContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * A test archive builder
 *
 * @author thomas.diesler@jboss.com
 * @since 22-Nov-2013
 */
public class ArchiveBuilder  {

    private final String name;
    private final Archive<?> archive;

    public ArchiveBuilder(String name) {
        this.name = name;
        if (getTargetContainer() == RuntimeType.TOMCAT) {
            archive = ShrinkWrap.create(WebArchive.class, name + ".war");
        } else {
            archive = ShrinkWrap.create(JavaArchive.class, name + ".jar");
        }
    }

    public String getName() {
        return name;
    }

    public static RuntimeType getTargetContainer () {
        return RuntimeType.getRuntimeType(System.getProperty("target.container"));
    }

    public ArchiveBuilder addClasses(Class<?>... classes) {
        ClassContainer<?> container = (ClassContainer<?>) archive;
        container.addClasses(classes);
        return this;
    }

    public ArchiveBuilder addPackage(Package pack) {
        ClassContainer<?> container = (ClassContainer<?>) archive;
        container.addPackage(pack);
        return this;
    }

    public ArchiveBuilder addClasses(RuntimeType target, Class<?>... classes) {
        if (getTargetContainer() == target) {
            ClassContainer<?> container = (ClassContainer<?>) archive;
            container.addClasses(classes);
        }
        return this;
    }

    public ArchiveBuilder addPackage(RuntimeType target, Package pack) {
        if (getTargetContainer() == target) {
            ClassContainer<?> container = (ClassContainer<?>) archive;
            container.addPackage(pack);
        }
        return this;
    }

    public ArchiveBuilder addAsResource(String resname) {
        if (archive instanceof WebContainer) {
            WebContainer<?> container = (WebContainer<?>) archive;
            container.addAsWebResource(resname, resname);
        } else {
            ResourceContainer<?> container = (ResourceContainer<?>) archive;
            container.addAsResource(resname);
        }
        return this;
    }

    public ArchiveBuilder setManifest(Asset manifestAsset) {
        ManifestContainer<?> container = (ManifestContainer<?>) archive;
        container.setManifest(manifestAsset);
        return this;
    }

    public Archive<?> getArchive() {
        //System.out.println(archive.toString(true));
        return archive;
    }
}
