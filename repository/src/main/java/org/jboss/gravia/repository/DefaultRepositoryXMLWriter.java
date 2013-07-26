/*
 * #%L
 * JBossOSGi Repository: API
 * %%
 * Copyright (C) 2011 - 2012 JBoss by Red Hat
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
package org.jboss.gravia.repository;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.gravia.repository.spi.AbstractRepositoryXMLWriter;


/**
 * Write repository contnet to XML.
 *
 * @author thomas.diesler@jboss.com
 * @since 21-May-2012
 */
public class DefaultRepositoryXMLWriter extends AbstractRepositoryXMLWriter {


    public DefaultRepositoryXMLWriter(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    protected XMLStreamWriter createXMLStreamWriter(OutputStream outputStream) {
        try {
            return XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot initialize repository writer", ex);
        }
    }

}
