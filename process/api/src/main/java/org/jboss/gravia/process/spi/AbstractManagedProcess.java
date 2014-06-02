/*
 * #%L
 * Fabric8 :: SPI
 * %%
 * Copyright (C) 2014 Red Hat
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

package org.jboss.gravia.process.spi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.jboss.gravia.process.api.ManagedProcess;
import org.jboss.gravia.process.api.ManagedProcessOptions;
import org.jboss.gravia.process.api.PortManager;
import org.jboss.gravia.repository.DefaultMavenDelegateRepository;
import org.jboss.gravia.repository.MavenDelegateRepository;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.runtime.LifecycleException;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * The managed root container
 *
 * @author thomas.diesler@jboss.com
 * @since 26-Feb-2014
 */
public abstract class AbstractManagedProcess<C extends ManagedProcessOptions> implements ManagedProcess<C> {

    private final MavenDelegateRepository mavenRepository;
    private final C createOptions;
    private File homeDir;
    private State state;
    private Process process;

    protected AbstractManagedProcess(C options, PropertiesProvider propsProvider) {
        IllegalArgumentAssertion.assertNotNull(options, "options");
        this.mavenRepository = new DefaultMavenDelegateRepository(propsProvider);
        this.createOptions = options;
    }

    @Override
    public C getCreateOptions() {
        return createOptions;
    }

    @Override
    public final synchronized void create() {
        IllegalStateAssertion.assertTrue(state == null, "Cannot create container in state: " + state);

        File targetDir = getCreateOptions().getTargetDirectory();
        IllegalStateAssertion.assertTrue(targetDir.isDirectory() || targetDir.mkdirs(), "Cannot create target dir: " + targetDir);

        for (MavenCoordinates artefact : getCreateOptions().getMavenCoordinates()) {
            Resource resource = mavenRepository.findMavenResource(artefact);
            IllegalStateAssertion.assertNotNull(resource, "Cannot find maven resource: " + artefact);

            ResourceContent content = resource.adapt(ResourceContent.class);
            IllegalStateAssertion.assertNotNull(content, "Cannot obtain resource content for: " + artefact);

            try {
                ArchiveInputStream ais;
                if ("tar.gz".equals(artefact.getType())) {
                    InputStream inputStream = content.getContent();
                    ais = new TarArchiveInputStream(new GZIPInputStream(inputStream));
                } else {
                    InputStream inputStream = content.getContent();
                    ais = new ArchiveStreamFactory().createArchiveInputStream(artefact.getType(), inputStream);
                }
                ArchiveEntry entry = null;
                boolean needContainerHome = homeDir == null;
                while ((entry = ais.getNextEntry()) != null) {
                    File targetFile;
                    if (needContainerHome) {
                        targetFile = new File(targetDir, entry.getName());
                    } else {
                        targetFile = new File(homeDir, entry.getName());
                    }
                    if (!entry.isDirectory()) {
                        File parentDir = targetFile.getParentFile();
                        IllegalStateAssertion.assertTrue(parentDir.exists() || parentDir.mkdirs(), "Cannot create target directory: " + parentDir);

                        FileOutputStream fos = new FileOutputStream(targetFile);
                        IOUtils.copy(ais, fos);
                        fos.close();

                        if (needContainerHome && homeDir == null) {
                            File currentDir = parentDir;
                            while (!currentDir.getParentFile().equals(targetDir)) {
                                currentDir = currentDir.getParentFile();
                            }
                            homeDir = currentDir;
                        }
                    }
                }
                ais.close();
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot extract artefact: " + artefact, ex);
            }
        }

        state = State.CREATED;

        try {
            doConfigure();
        } catch (Exception ex) {
            throw new LifecycleException("Cannot configure container", ex);
        }
    }

    @Override
    public File getHomeDir() {
        return homeDir;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public final synchronized void start() {
        assertNotDestroyed();
        try {
            if (state == State.CREATED || state == State.STOPPED) {
                doStart();
                state = State.STARTED;
            }
        } catch (Exception ex) {
            throw new LifecycleException("Cannot start container", ex);
        }
    }

    @Override
    public final synchronized void stop() {
        assertNotDestroyed();
        try {
            if (state == State.STARTED) {
                doStop();
                destroyProcess();
                state = State.STOPPED;
            }
        } catch (Exception ex) {
            throw new LifecycleException("Cannot stop container", ex);
        }
    }

    @Override
    public final synchronized void destroy() {
        assertNotDestroyed();
        if (state == State.STARTED) {
            try {
                stop();
            } catch (Exception ex) {
                // ignore
            }
        }
        try {
            doDestroy();
        } catch (Exception ex) {
            throw new LifecycleException("Cannot destroy container", ex);
        } finally {
            state = State.DESTROYED;
        }
    }

    private void assertNotDestroyed() {
        IllegalStateAssertion.assertFalse(state == State.DESTROYED, "Cannot start container in state: " + state);
    }

    protected void doConfigure() throws Exception {
    }

    protected void doStart() throws Exception {
    }

    protected void doStop() throws Exception {
    }

    protected void doDestroy() throws Exception {
    }

    protected void startProcess(ProcessBuilder processBuilder) throws IOException {
        process = processBuilder.start();
        new Thread(new ConsoleConsumer(process, getCreateOptions())).start();
    }

    protected void destroyProcess() throws Exception {
        if (process != null) {
            process.destroy();
            process.waitFor();
        }
    }

    protected final int nextAvailablePort(int portValue) {
        return nextAvailablePort(portValue, null);
    }

    protected int nextAvailablePort(int portValue, InetAddress bindAddr) {
        PortManager portManager = ServiceLocator.getRequiredService(PortManager.class);
        return portManager.nextAvailablePort(portValue, bindAddr);
    }

    /**
     * Runnable that consumes the output of the process.
     * If nothing consumes the output the container may hang on some platforms
     */
    public static class ConsoleConsumer implements Runnable {

        private final Process process;
        private final ManagedProcessOptions options;

        public ConsoleConsumer(Process process, ManagedProcessOptions options) {
            this.process = process;
            this.options = options;
        }

        @Override
        public void run() {
            final InputStream stream = process.getInputStream();
            try {
                byte[] buf = new byte[32];
                int num;
                // Do not try reading a line cos it considers '\r' end of line
                while ((num = stream.read(buf)) != -1) {
                    if (options.isOutputToConsole())
                        System.out.write(buf, 0, num);
                }
            } catch (IOException e) {
            }
        }
    }
}
