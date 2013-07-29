/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.repository;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.resource.Resource;

/**
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractRepositoryTest {

    protected RepositoryReader getRepositoryReader(String xmlres) throws XMLStreamException {
        InputStream input = getClass().getClassLoader().getResourceAsStream(xmlres);
        return new DefaultRepositoryXMLReader(input);
    }

    protected List<Resource> getResources(RepositoryReader reader) {
        List<Resource> resources = new ArrayList<Resource>();
        Resource resource = reader.nextResource();
        while (resource != null) {
            resources.add(resource);
            resource = reader.nextResource();
        }
        return resources;
    }

    protected void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File aux : file.listFiles())
                deleteRecursive(aux);
        }
        file.delete();
    }
}
