/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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

import static org.jboss.gravia.repository.spi.RepositoryLogger.LOGGER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.jboss.gravia.Constants;
import org.jboss.gravia.repository.spi.AbstractRepositoryStorage;
import org.jboss.gravia.repository.spi.RepositoryContentHelper;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * A simple {@link RepositoryStorage} that uses
 * the local file system.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class DefaultRepositoryStorage extends AbstractRepositoryStorage {

    public static final String REPOSITORY_XML_NAME = "repository.xml";

    private final File storageDir;
    private final File repoFile;

    public DefaultRepositoryStorage(PropertiesProvider propertyProvider, Repository repository) {
        super(repository);

        String filename = (String) propertyProvider.getProperty(Constants.PROPERTY_REPOSITORY_STORAGE_FILE, REPOSITORY_XML_NAME);
        Path storagePath = getRepositoryStoragePath(propertyProvider);

        storageDir = storagePath.toFile();
        repoFile = storagePath.resolve(filename).toFile();

        initRepositoryStorage();
    }

    public static Path getRepositoryStoragePath(PropertiesProvider propertyProvider) {
    	Runtime runtime = RuntimeLocator.getRuntime();
        String dirname = (String) propertyProvider.getProperty(Constants.PROPERTY_REPOSITORY_STORAGE_DIR);
        if (dirname == null && runtime != null) {
        	Module sysmodule = runtime.getModuleContext().getModule();
			dirname = sysmodule.getDataFile("repository").getPath();
        }
        if (dirname == null) {
        	dirname = Paths.get("repository").toString();
        	LOGGER.warn("Cannot obtain repository storage configuration, using: {}", dirname);
        }
    	return Paths.get(dirname);
    }
    
    @Override
    public RepositoryReader getPersistentRepositoryReader() throws RepositoryStorageException {
        try {
            return repoFile.exists() ? new DefaultRepositoryXMLReader(new FileInputStream(repoFile)) : null;
        } catch (IOException ex) {
            throw new RepositoryStorageException(ex);
        }
    }

    @Override
    public RepositoryWriter getPersistentRepositoryWriter() throws RepositoryStorageException {
        try {
            repoFile.getParentFile().mkdirs();
            return new DefaultRepositoryXMLWriter(new FileOutputStream(repoFile));
        } catch (IOException ex) {
            throw new RepositoryStorageException(ex);
        }
    }

    @Override
    public ResourceBuilder createResourceBuilder() {
        return new DefaultResourceBuilder();
    }

    @Override
    protected void addResourceContent(InputStream input, Map<String, Object> atts) throws RepositoryStorageException {
        try {
            // Copy the input stream to temporary storage
            File tempFile = new File(storageDir.getAbsolutePath() + File.separator + "temp-content");
            Long size = copyResourceContent(input, tempFile);
            atts.put(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE, size);
            // Calculate the SHA-256
            String sha256;
            String algorithm = RepositoryContentHelper.DEFAULT_DIGEST_ALGORITHM;
            try {
                sha256 = RepositoryContentHelper.getDigest(new FileInputStream(tempFile), algorithm);
                atts.put(ContentNamespace.CONTENT_NAMESPACE, sha256);
            } catch (NoSuchAlgorithmException ex) {
                throw new RepositoryStorageException("No such digest algorithm: " + algorithm, ex);
            }
            // Move the content to storage location
            String contentPath = sha256.substring(0, 2) + File.separator + sha256.substring(2) + File.separator + "content";
            File targetFile = new File(storageDir.getAbsolutePath() + File.separator + contentPath);
            targetFile.getParentFile().mkdirs();
            tempFile.renameTo(targetFile);
            URL url = targetFile.toURI().toURL();
            atts.put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, url);
        } catch (IOException ex) {
            throw new RepositoryStorageException(ex);
        }
    }

    @Override
    protected URL getBaseURL() {
        try {
            return storageDir.toURI().toURL();
        } catch (MalformedURLException e) {
            // ignore
            return null;
        }
    }

    private long copyResourceContent(InputStream input, File targetFile) throws IOException {
        int len = 0;
        long total = 0;
        byte[] buf = new byte[4096];
        targetFile.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream(targetFile);
        while ((len = input.read(buf)) >= 0) {
            out.write(buf, 0, len);
            total += len;
        }
        input.close();
        out.close();
        return total;
    }
}
