/*
 * #%L
 * JBossOSGi Framework
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.gravia.runtime.spi;

import static org.jboss.gravia.runtime.spi.AbstractRuntime.LOGGER;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Module;

/**
 * [TODO]
 * 
 * @ThreadSafe
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class RuntimeStorageHandler {

    private final File storageArea;

    public RuntimeStorageHandler(Map<String, Object> props, boolean firstInit) {

        // Create the storage area
        String dirName = (String) props.get(Constants.RUNTIME_STORAGE);
        if (dirName == null) {
            try {
                File storageDir = new File("./gravia-store");
                dirName = storageDir.getCanonicalPath();
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot create storage area", ex);
            }
        }
        storageArea = new File(dirName).getAbsoluteFile();

        // Cleanup the storage area
        String storageClean = (String) props.get(Constants.RUNTIME_STORAGE_CLEAN);
        if (firstInit == true && Constants.RUNTIME_STORAGE_CLEAN_ONFIRSTINIT.equals(storageClean)) {
            LOGGER.debugf("Deleting storage: %s", storageArea.getAbsolutePath());
            deleteRecursive(storageArea);
        }
    }

    public synchronized File getDataFile(Module module, String filename) {
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
