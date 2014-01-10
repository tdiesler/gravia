/*
 * #%L
 * Wildfly Gravia Subsystem
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

package org.wildfly.extension.gravia.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentHelper;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.server.Services;
import org.jboss.gravia.provision.DefaultResourceHandle;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.shrinkwrap.api.ConfigurationBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service providing the {@link ResourceInstaller}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 27-Jun-2013
 */
public class ResourceInstallerService extends AbstractService<ResourceInstaller> implements ResourceInstaller {

    static final Logger LOGGER = LoggerFactory.getLogger(GraviaConstants.class.getPackage().getName());

    private final InjectedValue<ServerEnvironment> injectedServerEnvironment = new InjectedValue<ServerEnvironment>();
    private final InjectedValue<ModelController> injectedController = new InjectedValue<ModelController>();
    private ServerDeploymentManager serverDeploymentManager;
    private ModelControllerClient modelControllerClient;

    public ServiceController<ResourceInstaller> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<ResourceInstaller> builder = serviceTarget.addService(GraviaConstants.RESOURCE_INSTALLER_SERVICE_NAME, this);
        builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, injectedServerEnvironment);
        builder.addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, injectedController);
        builder.addListener(verificationHandler);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        ModelController modelController = injectedController.getValue();
        modelControllerClient = modelController.createClient(Executors.newCachedThreadPool());
        serverDeploymentManager = ServerDeploymentManager.Factory.create(modelControllerClient);
    }

    @Override
    public void stop(StopContext context) {
        try {
            modelControllerClient.close();
        } catch (IOException ex) {
            // ignore
        }
    }

    @Override
    public ResourceInstaller getValue() throws IllegalStateException {
        return this;
    }

    @Override
    public synchronized ResourceHandle installResource(Resource res, Map<Requirement, Resource> mapping) throws ProvisionException {
        Capability icap = res.getIdentityCapability();
        boolean shared = Boolean.parseBoolean((String) icap.getAttributes().get(IdentityNamespace.CAPABILITY_SHARED_ATTRIBUTE));
        try {
            if (shared) {
                return installSharedResource(res, mapping);
            } else {
                return deployResource(res, mapping);
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (ProvisionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProvisionException("Cannot provision resource: " + res, ex);
        }
    }

    @SuppressWarnings("deprecation")
    private ResourceHandle installSharedResource(Resource res, Map<Requirement, Resource> mapping) throws Exception {
        LOGGER.info("Installing shared resource: {}", res);

        ResourceIdentity resid = res.getIdentity();
        File modulesDir = injectedServerEnvironment.getValue().getModulesDir();
        File moduleDir = new File(modulesDir, resid.getSymbolicName().replace(".", File.separator) + File.separator + "main");
        if (moduleDir.exists())
            throw new IllegalStateException("Module dir already exists: " + moduleDir);

        ResourceContent content = res.adapt(ResourceContent.class);
        if (content == null)
            throw new IllegalStateException("Cannot obtain repository content from: " + res);

        // copy resource content
        moduleDir.mkdirs();
        File resFile = new File(moduleDir, resid.getSymbolicName() + "-" + resid.getVersion() + ".jar");
        copyResourceContent(content.getContent(), resFile);

        // generate module.xml
        File xmlFile = new File(moduleDir, "module.xml");
        String moduleXML = generateModuleXML(resFile, res, mapping);
        FileOutputStream fos = new FileOutputStream(xmlFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(moduleXML);
        osw.close();

        return new DefaultResourceHandle(res) {
            @Override
            public void uninstall() {
                // cannot uninstall shared resource
            }
        };
    }

    private ResourceHandle deployResource(Resource res, Map<Requirement, Resource> mapping) throws Exception {
        LOGGER.info("Deploying resource: {}", res);
        final ServerDeploymentHelper serverDeployer = new ServerDeploymentHelper(serverDeploymentManager);
        final ResourceWrapper wrapper = getWrappedResourceContent(res, mapping);
        final String runtimeName = wrapper.getRuntimeName();
        serverDeployer.deploy(runtimeName, wrapper.getInputStream());
        return new DefaultResourceHandle(res) {
            @Override
            public void uninstall() {
                try {
                    serverDeployer.undeploy(runtimeName);
                } catch (Throwable th) {
                    LOGGER.warn("Cannot uninstall provisioned resource: " + getResource(), th);
                }
            }
        };
    }

    // Wrap the resource and add a generated jboss-deployment-structure.xml
    private ResourceWrapper getWrappedResourceContent(Resource res, Map<Requirement, Resource> mapping) {

        Capability icap = res.getIdentityCapability();
        String rtnameAtt = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE);
        String runtimeName = rtnameAtt != null ? rtnameAtt : res.getIdentity().getSymbolicName() + ".jar";

        // Do nothing if there is no mapping
        if (mapping == null) {
            InputStream content = res.adapt(ResourceContent.class).getContent();
            return new ResourceWrapper(runtimeName, content);
        }

        // Create content archive
        ConfigurationBuilder config = new ConfigurationBuilder().classLoaders(Collections.singleton(ShrinkWrap.class.getClassLoader()));
        JavaArchive archive = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, runtimeName);
        archive.as(ZipImporter.class).importFrom(res.adapt(ResourceContent.class).getContent());

        boolean wrapAsSubdeployment = runtimeName.endsWith(".war");

        // Create wrapper archive
        JavaArchive wrapper;
        Asset structureAsset;
        if (wrapAsSubdeployment) {
            wrapper = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, "wrapped-" + runtimeName + ".ear");
            structureAsset = new StringAsset(generateSubdeploymentDeploymentStructure(res, runtimeName, mapping));
        } else {
            wrapper = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, "wrapped-" + runtimeName);
            structureAsset = new StringAsset(generateDeploymentStructure(res, runtimeName, mapping));
        }
        wrapper.addAsManifestResource(structureAsset, "jboss-deployment-structure.xml");
        wrapper.add(archive, "/", ZipExporter.class);

        InputStream content = wrapper.as(ZipExporter.class).exportAsInputStream();
        return new ResourceWrapper(wrapper.getName(), content);
    }

    private String generateModuleXML(File resFile, Resource res, Map<Requirement, Resource> mapping) throws IOException {
        LOGGER.info("Generating dependencies for: {}", res);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<module xmlns='urn:jboss:module:1.1' name='" + res.getIdentity().getSymbolicName() + "'>");
        buffer.append(" <resources>");
        buffer.append("  <resource-root path='" + resFile.getName() + "'/>");
        buffer.append(" </resources>");
        addModuleDependencies(res, mapping, buffer);
        buffer.append("</module>");
        return buffer.toString();
    }

    private String generateDeploymentStructure(Resource res, String runtimeName, Map<Requirement, Resource> mapping) {
        LOGGER.info("Generating dependencies for: {}", res);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<jboss-deployment-structure xmlns='urn:jboss:deployment-structure:1.2'>");
        buffer.append(" <deployment>");
        buffer.append("  <resources>");
        buffer.append("   <resource-root path='" + runtimeName + "' use-physical-code-source='true'/>");
        buffer.append("  </resources>");
        addModuleDependencies(res, mapping, buffer);
        buffer.append(" </deployment>");
        buffer.append("</jboss-deployment-structure>");
        return buffer.toString();
    }

    private String generateSubdeploymentDeploymentStructure(Resource res, String runtimeName, Map<Requirement, Resource> mapping) {
        LOGGER.info("Generating dependencies for: {}", res);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<jboss-deployment-structure xmlns='urn:jboss:deployment-structure:1.2'>");
        buffer.append(" <sub-deployment name='" + runtimeName + "'>");
        buffer.append("  <resources>");
        buffer.append("   <resource-root path='" + runtimeName + "' use-physical-code-source='true'/>");
        buffer.append("  </resources>");
        addModuleDependencies(res, mapping, buffer);
        buffer.append(" </sub-deployment>");
        buffer.append("</jboss-deployment-structure>");
        return buffer.toString();
    }

    private void addModuleDependencies(Resource res, Map<Requirement, Resource> mapping, StringBuffer buffer) {
        buffer.append(" <dependencies>");
        for (Requirement req : res.getRequirements(IdentityNamespace.IDENTITY_NAMESPACE)) {
            Resource depres = mapping != null ? mapping.get(req) : null;
            if (depres != null) {
                String modname = depres.getIdentity().getSymbolicName();
                buffer.append("<module name='" + modname + "'/>");
                LOGGER.info("  {}", modname);
            }
        }
        buffer.append(" </dependencies>");
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

    static class ResourceWrapper {
        private final String runtimeName;
        private final InputStream inputStream;
        ResourceWrapper(String runtimeName, InputStream inputStream) {
            this.runtimeName = runtimeName;
            this.inputStream = inputStream;
        }
        String getRuntimeName() {
            return runtimeName;
        }
        InputStream getInputStream() {
            return inputStream;
        }
    }
}
