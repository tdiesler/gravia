package org.jboss.test.gravia.runtime.osgi.sub.a;

import org.junit.Assert;
import org.osgi.framework.Bundle;

public class OSGiTestHelper {

    public static Class<?> assertLoadClass(Bundle bundle, String className) {
        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException ex) {
            String message = "Unexpected ClassNotFoundException for: " + bundle.getSymbolicName() + " loads " + className;
            Assert.fail(message);
            return null;
        }
    }

    public static void assertLoadClassFail(Bundle bundle, String className) {
        try {
            Class<?> clazz = bundle.loadClass(className);
            String message = bundle.getSymbolicName() + " loads " + className;
            Assert.fail("ClassNotFoundException expected for: " + message + "\nLoaded from " + clazz.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // expected
        }
    }
    
}
