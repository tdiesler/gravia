/*
 * #%L
 * Gravia :: Container :: Tomcat :: Support
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

package org.jboss.gravia.container.tomcat.support;

import static org.jboss.gravia.container.tomcat.support.GraviaTomcatLogger.LOGGER;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.ant.DeployTask;
import org.apache.catalina.ant.UndeployTask;
import org.apache.catalina.users.MemoryUserDatabase;
import org.jboss.gravia.container.tomcat.extension.SharedModuleClassLoader;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.provision.spi.AbstractResourceHandle;
import org.jboss.gravia.provision.spi.AbstractResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.spi.NamedResourceAssociation;
import org.jboss.gravia.utils.IOUtils;
import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * Service providing the {@link ResourceInstaller}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 13-Jan-2014
 */
public class TomcatResourceInstaller extends AbstractResourceInstaller {

    private final static File catalinaHome = new File(SecurityActions.getSystemProperty("catalina.home", null));
    private final static File catalinaLib = new File(catalinaHome.getPath() + File.separator + "lib");
    private final static File catalinaTemp = new File(catalinaHome.getPath() + File.separator + "temp");

    private final static String TOMCAT_USER = "tomcat";

    private final UserDatabase userDatabase;
    private final RuntimeEnvironment environment;

    public TomcatResourceInstaller(RuntimeEnvironment environment) {
        this.environment = environment;
        try {
            userDatabase = new MemoryUserDatabase();
            userDatabase.open();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot open user database", ex);
        }
        if (userDatabase.findUser(TOMCAT_USER) == null)
            throw new IllegalStateException("Cannot obtain user: " + TOMCAT_USER);
    }

    @Override
    public RuntimeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public ResourceHandle installResourceProtected(Context context, Resource resource, boolean shared) throws Exception {
        ResourceHandle handle;
        if (shared) {
            handle = installSharedResourceInternal(context, resource);
        } else {
            handle = installUnsharedResourceInternal(context, resource);
        }
        return handle;
    }

    private ResourceHandle installSharedResourceInternal(Context context, Resource resource) throws Exception {
        LOGGER.info("Installing shared resource: {}", resource);

        ResourceIdentity resid = resource.getIdentity();
        ResourceContent content = resource.adapt(ResourceContent.class);
        IllegalStateAssertion.assertNotNull(content, "Cannot obtain content from: " + resource);

        // copy resource content
        File targetFile = new File(catalinaLib, resid.getSymbolicName() + "-" + resid.getVersion() + ".jar");
        if (targetFile.exists())
            throw new IllegalStateException("Module already exists: " + targetFile);

        IOUtils.copyStream(content.getContent(), new FileOutputStream(targetFile));

        // Install the shared module
        Module module = installSharedResource(resource, targetFile);

        // Start the module
        module.start();

        Resource modres = module.adapt(Resource.class);
        return new AbstractResourceHandle(modres, module) {
            @Override
            public void uninstall() {
                LOGGER.info("Cannot uninstall shared resource: {}", getResource());
            }
        };
    }

    private ResourceHandle installUnsharedResourceInternal(Context context, Resource resource) throws Exception {
        LOGGER.info("Installing unshared resource: {}", resource);

        File tempfile = null;
        ResourceIdentity identity = resource.getIdentity();
        ContentCapability ccap = (ContentCapability) resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE).get(0);
        URL contentURL = ccap.getContentURL();
        if (contentURL == null) {
            ResourceContent content = resource.adapt(ResourceContent.class);
            IllegalStateAssertion.assertNotNull(content, "Cannot obtain content from: " + resource);
            tempfile = new File(catalinaTemp, identity.getSymbolicName() + "-" + identity.getVersion() + ".war");
            IOUtils.copyStream(content.getContent(), new FileOutputStream(tempfile));
            contentURL = tempfile.toURI().toURL();
        }

        // Get contextPath, username, password
        final String contextPath = getContextPath(resource);
        final User user = userDatabase.findUser(TOMCAT_USER);
        final String password = user.getPassword();

        NamedResourceAssociation.putResource(contextPath, resource);
        try {
            DeployTask task = new DeployTask();
            task.setWar(contentURL.toExternalForm());
            task.setUsername(user.getName());
            task.setPassword(password);
            task.setPath(contextPath);
            task.execute();
        } finally {
            NamedResourceAssociation.removeResource(contextPath);
            if (tempfile != null) {
                tempfile.delete();
            }
        }

        // Get the resource as module (may be null)
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(identity);

        Resource modres = module != null ? module.adapt(Resource.class) : resource;
        return new AbstractResourceHandle(modres, module) {
            @Override
            public void uninstall() {
                UndeployTask task = new UndeployTask();
                task.setUsername(user.getName());
                task.setPassword(password);
                task.setPath(contextPath);
                task.execute();
            }
        };
    }

    private Module installSharedResource(Resource resource, File targetFile) throws Exception {

        // Get a resource copy with updated content capability
        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        for (Capability cap : resource.getCapabilities(null)) {
            String namespace = cap.getNamespace();
            if (!ContentNamespace.CONTENT_NAMESPACE.equals(namespace)) {
                builder.addCapability(namespace, cap.getAttributes(), cap.getDirectives());
            }
        }
        builder.addContentCapability(targetFile.toURI().toURL());
        for (Requirement req : resource.getRequirements(null)) {
            builder.addRequirement(req.getNamespace(), req.getAttributes(), req.getDirectives());
        }
        resource = builder.getResource();

        // Add the module to the {@link SharedModuleClassLoader}
        SharedModuleClassLoader.addSharedModule(resource);

        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        ClassLoader classLoader = SharedModuleClassLoader.class.getClassLoader();
        return runtime.installModule(classLoader, resource, null);
    }

    private String getContextPath(Resource res) {
        String contextPath = (String) res.getIdentityCapability().getAttribute("contextPath");
        if (contextPath == null)
            contextPath = res.getIdentity().getSymbolicName();
        return "/" + contextPath;
    }
}
