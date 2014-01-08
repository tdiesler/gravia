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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentHelper;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.server.Services;
import org.jboss.gravia.provision.DefaultResourceHandle;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
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

    private final InjectedValue<ModelController> injectedController = new InjectedValue<ModelController>();
    private ServerDeploymentManager serverDeploymentManager;
    private ModelControllerClient modelControllerClient;

    public ServiceController<ResourceInstaller> install(ServiceTarget serviceTarget, ServiceVerificationHandler verificationHandler) {
        ServiceBuilder<ResourceInstaller> builder = serviceTarget.addService(GraviaConstants.RESOURCE_INSTALLER_SERVICE_NAME, this);
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
    public ResourceHandle installResource(Resource res, Map<Requirement, Resource> mapping) throws ProvisionException {

        RepositoryContent content = res.adapt(RepositoryContent.class);
        if (content == null) {
            return new DefaultResourceHandle(res);
        }

        res.getIdentityCapability().getAttributes().get("");
        final String runtimeName = res.getIdentity().getSymbolicName();
        final ServerDeploymentHelper serverDeployer = new ServerDeploymentHelper(serverDeploymentManager);
        try {
            InputStream input = getWrappedResourceContent(res, mapping);
            serverDeployer.deploy(runtimeName, input);
        } catch (Throwable th) {
            throw new ProvisionException("Cannot provision resource: " + res, th);
        }

        return new DefaultResourceHandle(res) {

            @Override
            public void uninstall() throws ProvisionException {
                try {
                    serverDeployer.undeploy(runtimeName);
                } catch (Throwable th) {
                    throw new ProvisionException("Cannot uninstall provisioned resource: " + getResource(), th);
                }
            }
        };
    }

    // Wrap the resource and add a generated jboss-deployment-structure.xml
    private InputStream getWrappedResourceContent(Resource res, Map<Requirement, Resource> mapping) {
        ResourceIdentity resid = res.getIdentity();
        ConfigurationBuilder config = new ConfigurationBuilder().classLoaders(Collections.singleton(ShrinkWrap.class.getClassLoader()));
        JavaArchive archive = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, "wrapped-resource.jar");
        archive.as(ZipImporter.class).importFrom(((RepositoryContent) res).getContent());
        JavaArchive wrapper = ShrinkWrap.createDomain(config).getArchiveFactory().create(JavaArchive.class, "wrapped:" + resid.getSymbolicName());
        wrapper.addAsManifestResource(getDeploymentStructureAsset(res, mapping), "jboss-deployment-structure.xml");
        wrapper.add(archive, "/", ZipExporter.class);
        return wrapper.as(ZipExporter.class).exportAsInputStream();
    }

    private Asset getDeploymentStructureAsset(Resource res, Map<Requirement, Resource> mapping) {
        LOGGER.info("Generating dependencies for: {}", res);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<jboss-deployment-structure xmlns='urn:jboss:deployment-structure:1.2'>");
        buffer.append(" <deployment>");
        buffer.append("  <resources>");
        buffer.append("   <resource-root path='wrapped-resource.jar' use-physical-code-source='true'/>");
        buffer.append("  </resources>");
        buffer.append("  <dependencies>");
        for (Requirement req : res.getRequirements(IdentityNamespace.IDENTITY_NAMESPACE)) {
            Resource depres = mapping.get(req);
            if (depres != null) {
                Capability icap = depres.getIdentityCapability();
                String type = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE);
                String modname = depres.getIdentity().getSymbolicName();
                if (!IdentityNamespace.TYPE_MODULE.equals(type)) {
                    modname = "deployment." + modname;
                }
                buffer.append("<module name='" + modname + "'/>");
                LOGGER.info("  {}", modname);
            }
        }
        buffer.append("  </dependencies>");
        buffer.append(" </deployment>");
        buffer.append("</jboss-deployment-structure>");
        return new StringAsset(buffer.toString());
    }
}
