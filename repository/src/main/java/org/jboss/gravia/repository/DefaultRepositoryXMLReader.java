/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
package org.jboss.gravia.repository;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.jboss.gravia.repository.spi.AbstractRepositoryXMLReader;
import org.jboss.gravia.resource.ResourceBuilder;


/**
 * Read repository contnet from XML.
 *
 * @author thomas.diesler@jboss.com
 * @since 21-May-2012
 */
public class DefaultRepositoryXMLReader extends AbstractRepositoryXMLReader {

    public DefaultRepositoryXMLReader(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    protected XMLStreamReader createXMLStreamReader(InputStream inputStream) {
        try {
            return XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot initialize repository reader", ex);
        }
    }

    @Override
    protected ResourceBuilder createResourceBuilder() {
        return new DefaultRepositoryResourceBuilder();
    }
}
