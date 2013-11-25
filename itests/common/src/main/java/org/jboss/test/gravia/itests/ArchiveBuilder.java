/*
 * #%L
 * JBossOSGi SPI
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.itests;

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
 * @author thomas.diesler@jbos.com
 * @since 22-Nov-2013
 */
public class ArchiveBuilder  {

    private final String name;
    private final Archive<?> archive;

    public enum TargetContainer {
        karaf, tomcat, wildfly
    }

    public ArchiveBuilder(String name) {
        this.name = name;
        if (getTargetContainer() == TargetContainer.tomcat) {
            archive = ShrinkWrap.create(WebArchive.class, name + ".war");
        } else {
            archive = ShrinkWrap.create(JavaArchive.class, name + ".jar");
        }
    }

    public String getName() {
        return name;
    }

    public TargetContainer getTargetContainer () {
        return TargetContainer.valueOf(System.getProperty("target.container"));
    }

    public ArchiveBuilder addClasses(Class<?>... classes) {
        ClassContainer<?> container = (ClassContainer<?>) archive;
        container.addClasses(classes);
        return this;
    }

    public ArchiveBuilder addClasses(TargetContainer target, Class<?>... classes) {
        if (getTargetContainer() == target) {
            ClassContainer<?> container = (ClassContainer<?>) archive;
            container.addClasses(classes);
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
