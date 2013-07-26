package org.jboss.test.gravia.repository;
/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.jboss.gravia.repository.DefaultRepositoryXMLReader;
import org.jboss.gravia.repository.DefaultRepositoryXMLWriter;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryWriter;
import org.jboss.gravia.resource.Resource;
import org.junit.Test;

/** 
 * Test the repository reader/writer
 *
 * @author thomas.diesler@jboss.com
 * @since 21-May-2012
 */
public class AbstractResourcesWriterTestCase extends AbstractRepositoryTest {

    @Test
    public void testXMLWriter() throws Exception {

        RepositoryReader reader = getRepositoryReader("xml/abstract-resources.xml");
        Map<String, String> attributes = reader.getRepositoryAttributes();
        List<Resource> resources = getResources(reader);

        File file = new File("target/abstract-resources.xml");
        RepositoryWriter writer = new DefaultRepositoryXMLWriter(new FileOutputStream(file));
        writer.writeRepositoryElement(attributes);
        for (Resource res : resources) {
            writer.writeResource(res);
        }
        writer.close();

        reader = new DefaultRepositoryXMLReader(new FileInputStream(file));
        AbstractResourcesReaderTestCase.verifyContent(reader.getRepositoryAttributes(), getResources(reader));
    }
}
