Fuse Portable Runtime
=====================

Gravia is the Fuse Portable Runtime. Traditionally tied to OSGi, Gravia is designed to make the Fuse Integration Platform available on a set of supported target containers. 

Supported Target Containers
---------------------------

The set of supported target containers includes but is not limited to 

* [Apache Karaf](http://karaf.apache.org/)
* [JBoss WildFly](http://www.wildfly.org/)

Goals
-----

* Provide a Dynamic Services API as the basis of component integration
* Provide [Declarative Services](http://felix.apache.org/documentation/subprojects/apache-felix-service-component-runtime.html) and [Configuration Admin](http://felix.apache.org/documentation/subprojects/apache-felix-config-admin.html) functionality
* Provide a modularity layer that works with the modularity concepts provided by the target container
* Provide an abstraction for Resources with associated Capabilities and Requirements
* Provide an Resolver that can find a consistent solution given a set of Capabilities and Requirements
* Provide a Repository of actual Resource artefacts as well as abstract Feature Resources
* Provide a Provisioner that can provision the Runtime using the Repository and Resolver
* Provide a deployment model that works across all supported target containers

Runtime Design
--------------

The Gravia Runtime comes in two flavours

* [Embedded Runtime](../../wiki/Runtime-Design#embedded-runtime)
* [OSGi Runtime](../../wiki/Runtime-Design#osgi-runtime)

The Embedded Runtime is used with target containers that do not natively support OSGi (e.g. Wildfly). 
The OSGi Runtime is used on OSGi containers (e.g. Apache Felix, Equinox)

Links
-----

* [Gravia API](http://tdiesler.github.io/gravia/1.1/apidocs)
* [OSGi Integration](../../wiki/OSGi-Integration), [Provisioning](../../wiki/OSGi-Provisioning)
* [WildFly Integration](../../wiki/WildFly-Integration), [Provisioning](../../wiki/WildFly-Provisioning)
