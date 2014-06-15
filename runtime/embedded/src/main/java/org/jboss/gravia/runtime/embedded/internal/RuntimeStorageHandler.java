/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
package org.jboss.gravia.runtime.embedded.internal;


import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.io.File;
import java.io.IOException;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * The runtime file storage handler.
 *
 * @ThreadSafe
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class RuntimeStorageHandler {

    private final File storageArea;

    RuntimeStorageHandler(PropertiesProvider props, boolean firstInit) {

        // Create the storage area
        String dirName = (String) props.getProperty(org.jboss.gravia.Constants.RUNTIME_STORAGE_DIR);
        if (dirName == null) {
            try {
                File storageDir = new File("." + File.separator + org.jboss.gravia.Constants.RUNTIME_STORAGE_DEFAULT);
                dirName = storageDir.getCanonicalPath();
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot create storage area", ex);
            }
        }
        storageArea = new File(dirName).getAbsoluteFile();

        // Cleanup the storage area
        String storageClean = (String) props.getProperty(org.jboss.gravia.Constants.RUNTIME_STORAGE_CLEAN);
        if (firstInit == true && org.jboss.gravia.Constants.RUNTIME_STORAGE_CLEAN_ONFIRSTINIT.equals(storageClean)) {
            LOGGER.debug("Deleting storage: {}", storageArea.getAbsolutePath());
            deleteRecursive(storageArea);
        }
    }

    File getDataFile(Module module, String filename) {
        File moduleDir = getStorageDir(module);
        File dataFile = new File(moduleDir.getAbsolutePath() + File.separator + filename);
        dataFile.getParentFile().mkdirs();

        String filePath = dataFile.getAbsolutePath();
        try {
            filePath = dataFile.getCanonicalPath();
        } catch (IOException ex) {
            // ignore
        }
        return new File(filePath);
    }

    private File getStorageDir(Module module) {
        String identity = module.getIdentity().toString().replace(':', '-').replace('/', '-');
        File moduleDir = new File(storageArea + "/module-" + identity);
        if (moduleDir.exists() == false)
            moduleDir.mkdirs();

        String filePath = moduleDir.getAbsolutePath();
        try {
            filePath = moduleDir.getCanonicalPath();
        } catch (IOException ex) {
            // ignore
        }
        return new File(filePath);
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File aux : file.listFiles())
                deleteRecursive(aux);
        }
        file.delete();
    }
}
