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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.gravia.repository.Repository.ConfigurationPropertyProvider;
import org.jboss.gravia.repository.spi.FilesystemRepositoryStorage;
import org.jboss.gravia.resource.ResourceBuilder;


/**
 * A simple {@link RepositoryStorage} that uses
 * the local file system.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class DefaultFilesystemRepositoryStorage extends FilesystemRepositoryStorage {

    public DefaultFilesystemRepositoryStorage(Repository repository, File storageDir, ConfigurationPropertyProvider propProvider) {
        super(repository, storageDir, propProvider);
    }

    @Override
    protected RepositoryReader createRepositoryReader(InputStream inputStream) {
        return new DefaultRepositoryXMLReader(inputStream);
    }

    @Override
    protected RepositoryWriter createRepositoryWriter(OutputStream outputStream) {
        return new DefaultRepositoryXMLWriter(outputStream);
    }

    @Override
    protected ResourceBuilder createResourceBuilder() {
        return new DefaultRepositoryResourceBuilder();
    }
}
