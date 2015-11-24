/*
 * #%L
 * Gravia :: Resource
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
package org.jboss.gravia;

/**
 * Defines standard names for the environment system properties, service
 * properties, and Manifest header attribute keys.
 * <p>
 * The values associated with these keys are of type {@code String}, unless
 * otherwise indicated.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Constants  {

    /**
     * Manifest header to mark an OSGi Bundle as Gravia enabled.
     * The module identity is derived from the Bundle-SymbolicName and Bundle-Version.
     */
    public static final String GRAVIA_ENABLED = "Gravia-Enabled";

    /**
     * Manifest header that defines the module's identitiy.
     */
    public static final String GRAVIA_IDENTITY_CAPABILITY = "Gravia-Identity";

    /**
     * Manifest header that defines a module identitiy requirement.
     */
    public static final String GRAVIA_IDENTITY_REQUIREMENT = "Gravia-IdentityRequirement";

    /**
     * Manifest header that defines a generic module capability.
     */
    public static final String GRAVIA_CAPABILITY = "Gravia-Capability";
    /**
     * Manifest header that defines a generic module requirement.
     */
    public static final String GRAVIA_REQUIREMENT = "Gravia-Requirement";

    /**
     * Header attribute identifying the module's activator class.
     *
     * <p>
     * If present, this header specifies the name of the module resource class
     * that implements the {@code ModuleActivator} interface and whose
     * {@code start} and {@code stop} methods are called by the Runtime when
     * the module is started and stopped, respectively.
     *
     * <p>
     * The header value may be retrieved from the {@code Dictionary} object
     * returned by the {@code Module.getHeaders()} method.
     */
    String MODULE_ACTIVATOR = "Module-Activator";

    /**
     * The default configuration file name as well as the
     * system property to discover it.
     */
    String GRAVIA_PROPERTIES = "gravia.properties";

    /**
     * Runtime property specifying the persistent storage area used
     * by the runtime. The value of this property must be a valid file path in
     * the file system to a directory. If the specified directory does not exist
     * then the runtime will create the directory. If the specified path
     * exists but is not a directory or if the runtime fails to create the
     * storage directory, then runtime initialization must fail. The runtime
     * is free to use this directory as it sees fit. This area can not be shared
     * with anything else.
     * <p>
     * If this property is not set, the runtime should use a reasonable
     * platform default for the persistent storage area.
     */
    String RUNTIME_STORAGE_DIR = "org.jboss.gravia.runtime.storage.dir";

    /**
     * Runtime property specifying if and when the persistent
     * storage area for the runtime should be cleaned. If this property is not
     * set, then the runtime storage area must not be cleaned.
     */
    String RUNTIME_STORAGE_CLEAN = "org.jboss.gravia.runtime.storage.clean";

    /**
     * The property that defines the configurations directory.
     */
    String RUNTIME_CONFIGURATIONS_DIR = "org.jboss.gravia.runtime.configurations.dir";

    /**
     * Specifies that the runtime storage area must be cleaned before the
     * runtime is initialized for the first time. Subsequent starts of the
     * runtime will not result in cleaning the runtime storage area.
     */
    String RUNTIME_STORAGE_CLEAN_ONFIRSTINIT = "onFirstInit";

    /**
     * The default storage location
     */
    String RUNTIME_STORAGE_DEFAULT = "gravia-store";

    /**
     * A string value representing the type of the runtime (e.g. wildfly,karaf,etc)
     */
    String RUNTIME_TYPE = "org.jboss.gravia.runtime.type";

    /**
     * Service property identifying all of the class names under which a service
     * was registered in the Runtime. The value of this property must be of
     * type {@code String[]}.
     *
     * <p>
     * This property is set by the Runtime when a service is registered.
     */
    String OBJECTCLASS = "objectClass";

    /**
     * Service property identifying a service's registration number. The value
     * of this property must be of type {@code Long}.
     *
     * <p>
     * The value of this property is assigned by the Runtime when a service is
     * registered. The Runtime assigns a unique value that is larger than all
     * previously assigned values since the Runtime was started. These values
     * are NOT persistent across restarts of the Runtime.
     */
    String SERVICE_ID = "service.id";

    /**
     * Service property identifying a service's persistent identifier.
     *
     * <p>
     * This property may be supplied in the {@code properties}
     * {@code Dictionary} object passed to the
     * {@code ModuleContext.registerService} method. The value of this property
     * must be of type {@code String}, {@code String[]}, or {@code Collection}
     * of {@code String}.
     *
     * <p>
     * A service's persistent identifier uniquely identifies the service and
     * persists across multiple Runtime invocations.
     */
    String SERVICE_PID = "service.pid";

    /**
     * Service property identifying a service's ranking number.
     *
     * <p>
     * This property may be supplied in the {@code properties
     * Dictionary} object passed to the {@code ModuleContext.registerService}
     * method. The value of this property must be of type {@code Integer}.
     *
     * <p>
     * The service ranking is used by the Runtime to determine the <i>natural
     * order</i> of services, see {code ServiceReference#compareTo(Object)},
     * and the <i>default</i> service to be returned from a call to the
     * {@code ModuleContext#getServiceReference(Class)} or
     * {@code ModuleContext#getServiceReference(String)} method.
     *
     * <p>
     * The default ranking is zero (0). A service with a ranking of
     * {@code Integer.MAX_VALUE} is very likely to be returned as the default
     * service, whereas a service with a ranking of {@code Integer.MIN_VALUE} is
     * very unlikely to be returned.
     *
     * <p>
     * If the supplied property value is not of type {@code Integer}, it is
     * deemed to have a ranking value of zero.
     */
    String SERVICE_RANKING = "service.ranking";

    /**
     * Service property naming the protocols serviced by a URLStreamHandlerService.
     * The property's value is a protocol name.
     */
    String URL_HANDLER_PROTOCOL    = "url.handler.protocol";

    /**
     * The property that defines the Maven Repository base URLs.
     */
    String PROPERTY_MAVEN_REPOSITORY_BASE_URLS = "org.jboss.gravia.repository.maven.base.urls";

    /**
     * The property that defines the repository storage directory.
     */
    String PROPERTY_REPOSITORY_STORAGE_DIR = "org.jboss.gravia.repository.storage.dir";

    /**
     * The property that defines the repository storage file.
     */
    String PROPERTY_REPOSITORY_STORAGE_FILE = "org.jboss.gravia.repository.storage.file";
}
