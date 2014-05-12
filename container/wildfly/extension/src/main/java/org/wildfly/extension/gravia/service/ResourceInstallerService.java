/*
 * #%L
 * Gravia :: Container :: WildFly :: Extension
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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

package org.wildfly.extension.gravia.service;

import static org.wildfly.extension.gravia.GraviaExtensionLogger.LOGGER;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.gravia.provision.DefaultResourceHandle;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.provision.spi.AbstractResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.spi.NamedResourceAssociation;
import org.jboss.gravia.utils.IOUtils;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.Service;
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
import org.wildfly.extension.gravia.GraviaConstants;

/**
 * Service providing the {@link ResourceInstaller}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 27-Jun-2013
 */
public class ResourceInstallerService extends AbstractResourceInstaller implements Service<ResourceInstaller> {

    private final InjectedValue<ServerEnvironment> injectedServerEnvironment = new InjectedValue<ServerEnvironment>();
    private final InjectedValue<ServiceModuleLoader> injectedServiceModuleLoader = new InjectedValue<ServiceModuleLoader>();
    private final InjectedValue<ModelController> injectedController = new InjectedValue<ModelController>();
    private final InjectedValue<ModuleContext> injectedModuleContext = new InjectedValue<ModuleContext>();
    private final InjectedValue<RuntimeEnvironment> injectedEnvironment = new InjectedValue<RuntimeEnvironment>();
    private ServerDeploymentManager serverDeploymentManager;
    private ModelControllerClient modelControllerClient;
    private ServiceRegistration<?> registration;

    public ServiceController<ResourceInstaller> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<ResourceInstaller> builder = serviceTarget.addService(GraviaConstants.RESOURCE_INSTALLER_SERVICE_NAME, this);
        builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, injectedServerEnvironment);
        builder.addDependency(GraviaConstants.ENVIRONMENT_SERVICE_NAME, RuntimeEnvironment.class, injectedEnvironment);
        builder.addDependency(GraviaConstants.MODULE_CONTEXT_SERVICE_NAME, ModuleContext.class, injectedModuleContext);
        builder.addDependency(Services.JBOSS_SERVICE_MODULE_LOADER, ServiceModuleLoader.class, injectedServiceModuleLoader);
        builder.addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, injectedController);
        builder.addListener(verificationHandler);
        return builder.install();
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        ModelController modelController = injectedController.getValue();
        modelControllerClient = modelController.createClient(Executors.newCachedThreadPool());
        serverDeploymentManager = ServerDeploymentManager.Factory.create(modelControllerClient);

        ModuleContext syscontext = injectedModuleContext.getValue();
        registration = syscontext.registerService(ResourceInstaller.class, this, null);
    }

    @Override
    public void stop(StopContext context) {
        if (registration != null) {
            registration.unregister();
        }
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
    public RuntimeEnvironment getEnvironment() {
        return injectedEnvironment.getValue();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceHandle processSharedResource(Context context, Resource resource) throws Exception {
        LOGGER.info("Installing shared resource: {}", resource);

        ResourceIdentity identity = resource.getIdentity();
        String symbolicName = identity.getSymbolicName();
        Version version = identity.getVersion();
        File modulesDir = injectedServerEnvironment.getValue().getModulesDir();
        File moduleDir = new File(modulesDir, symbolicName.replace(".", File.separator) + File.separator + version);
        if (moduleDir.exists())
            throw new IllegalStateException("Module dir already exists: " + moduleDir);

        ResourceContent content = resource.adapt(ResourceContent.class);
        if (content == null)
            throw new IllegalStateException("Cannot obtain repository content from: " + resource);

        // copy resource content
        moduleDir.mkdirs();
        File resFile = new File(moduleDir, symbolicName + "-" + version + ".jar");
        IOUtils.copyStream(content.getContent(), new FileOutputStream(resFile));

        String slot = version != Version.emptyVersion ? version.toString() : "main";
        ModuleIdentifier modid = ModuleIdentifier.create(symbolicName, slot);

        // generate module.xml
        File xmlFile = new File(moduleDir, "module.xml");
        Map<Requirement, Resource> mapping = context.getResourceMapping();
        String moduleXML = generateModuleXML(resFile, resource, modid, mapping);
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(xmlFile));
        osw.write(moduleXML);
        osw.close();

        // generate the main slot
        File mainDir = new File(moduleDir.getParentFile(), "main");
        if (!mainDir.exists()) {
            mainDir.mkdirs();
            File mainFile = new File(mainDir, "module.xml");
            moduleXML = generateModuleAliasXML(resource, modid);
            osw = new OutputStreamWriter(new FileOutputStream(mainFile));
            osw.write(moduleXML);
            osw.close();
        }

        // Install the resource as module
        ModuleLoader moduleLoader = org.jboss.modules.Module.getBootModuleLoader();
        ModuleClassLoader classLoader = moduleLoader.loadModule(modid).getClassLoader();
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.installModule(classLoader, resource, null);

        return new DefaultResourceHandle(resource, module) {
            @Override
            public void uninstall() {
                // cannot uninstall shared resource
            }
        };
    }

    @Override
    public ResourceHandle processUnsharedResource(Context context, Resource resource) throws Exception {
        LOGGER.info("Installing unshared resource: {}", resource);

        final ServerDeploymentHelper serverDeployer = new ServerDeploymentHelper(serverDeploymentManager);
        final ResourceWrapper wrapper = getWrappedResourceContent(resource, context.getResourceMapping());

        String runtimeName = wrapper.getRuntimeName();
        NamedResourceAssociation.putResource(runtimeName, resource);
        try {
            String deploymentName = wrapper.getDeploymentName();
            serverDeployer.deploy(deploymentName, wrapper.getInputStream());
        } finally {
            NamedResourceAssociation.removeResource(runtimeName);
        }

        // Install the resource as module if it has not happend already
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(resource.getIdentity());
        if (module == null) {
            ModuleLoader moduleLoader = injectedServiceModuleLoader.getValue();
            ModuleIdentifier modid = ModuleIdentifier.create(ServiceModuleLoader.MODULE_PREFIX + runtimeName);
            ClassLoader classLoader = moduleLoader.loadModule(modid).getClassLoader();
            module = runtime.installModule(classLoader, resource, null);
        }

        Resource modres = module != null ? module.adapt(Resource.class) : resource;
        return new DefaultResourceHandle(modres, module) {
            @Override
            public void uninstall() {
                try {
                    String deploymentName = wrapper.getDeploymentName();
                    serverDeployer.undeploy(deploymentName);
                } catch (Throwable th) {
                    LOGGER.warn("Cannot uninstall provisioned resource: " + getResource(), th);
                }
            }
        };
    }

    // Wrap the resource and add a generated jboss-deployment-structure.xml
    private ResourceWrapper getWrappedResourceContent(Resource resource, Map<Requirement, Resource> mapping) {

        Capability icap = resource.getIdentityCapability();
        String rtnameAtt = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE);
        String runtimeName = rtnameAtt != null ? rtnameAtt : resource.getIdentity().getSymbolicName() + ".jar";

        // Do nothing if there is no mapping
        if (mapping == null || mapping.isEmpty()) {
            InputStream content = resource.adapt(ResourceContent.class).getContent();
            return new ResourceWrapper(runtimeName, runtimeName, content);
        }

        // Create content archive
        ConfigurationBuilder config = new ConfigurationBuilder().classLoaders(Collections.singleton(ShrinkWrap.class.getClassLoader()));
        JavaArchive archive = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, runtimeName);
        archive.as(ZipImporter.class).importFrom(resource.adapt(ResourceContent.class).getContent());

        boolean wrapAsSubdeployment = runtimeName.endsWith(".war");

        // Create wrapper archive
        JavaArchive wrapper;
        Asset structureAsset;
        if (wrapAsSubdeployment) {
            wrapper = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, "wrapped-" + runtimeName + ".ear");
            structureAsset = new StringAsset(generateSubdeploymentDeploymentStructure(resource, runtimeName, mapping));
        } else {
            wrapper = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, "wrapped-" + runtimeName);
            structureAsset = new StringAsset(generateDeploymentStructure(resource, runtimeName, mapping));
        }
        wrapper.addAsManifestResource(structureAsset, "jboss-deployment-structure.xml");
        wrapper.add(archive, "/", ZipExporter.class);

        InputStream content = wrapper.as(ZipExporter.class).exportAsInputStream();
        return new ResourceWrapper(wrapper.getName(), runtimeName, content);
    }

    private String generateModuleXML(File resFile, Resource resource, ModuleIdentifier modid, Map<Requirement, Resource> mapping) throws IOException {
        LOGGER.info("Generating dependencies for: {}", resource);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<module xmlns='urn:jboss:module:1.3' name='" + modid.getName() + "' slot='" + modid.getSlot() + "'>");
        buffer.append(" <resources>");
        buffer.append("  <resource-root path='" + resFile.getName() + "'/>");
        buffer.append(" </resources>");
        addModuleDependencies(resource, mapping, buffer);
        buffer.append("</module>");
        return buffer.toString();
    }

    private String generateModuleAliasXML(Resource resource, ModuleIdentifier modid) throws IOException {
        LOGGER.info("Generating main alias for: {}", resource);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<module-alias xmlns='urn:jboss:module:1.3' name='" + modid.getName() + "' target-name='" + modid.getName() + "' target-slot='" + modid.getSlot() + "'/>");
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

    private String generateSubdeploymentDeploymentStructure(Resource resource, String runtimeName, Map<Requirement, Resource> mapping) {
        LOGGER.info("Generating dependencies for: {}", resource);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<jboss-deployment-structure xmlns='urn:jboss:deployment-structure:1.2'>");
        buffer.append(" <sub-deployment name='" + runtimeName + "'>");
        buffer.append("  <resources>");
        buffer.append("   <resource-root path='" + runtimeName + "' use-physical-code-source='true'/>");
        buffer.append("  </resources>");
        addModuleDependencies(resource, mapping, buffer);
        buffer.append(" </sub-deployment>");
        buffer.append("</jboss-deployment-structure>");
        return buffer.toString();
    }

    private void addModuleDependencies(Resource resource, Map<Requirement, Resource> mapping, StringBuffer buffer) {
        buffer.append(" <dependencies>");
        for (Requirement req : resource.getRequirements(IdentityNamespace.IDENTITY_NAMESPACE)) {
            Resource depres = mapping != null ? mapping.get(req) : null;
            if (depres != null) {
                ResourceIdentity identity = depres.getIdentity();
                String modname = identity.getSymbolicName();
                Version version = identity.getVersion();
                String slot = version != Version.emptyVersion ? "slot='" + version + "'" : "";
                buffer.append("<module name='" + modname + "' " + slot + "/>");
                LOGGER.info("  {}", identity);
            }
        }
        buffer.append(" </dependencies>");
    }

    static class ResourceWrapper {
        private final String deploymentName;
        private final String runtimeName;
        private final InputStream inputStream;
        ResourceWrapper(String deploymentName, String runtimeName, InputStream inputStream) {
            this.deploymentName = deploymentName;
            this.runtimeName = runtimeName;
            this.inputStream = inputStream;
        }
        String getDeploymentName() {
            return deploymentName;
        }
        String getRuntimeName() {
            return runtimeName;
        }
        InputStream getInputStream() {
            return inputStream;
        }
    }
}
