/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.aries.jmx;

import javax.management.StandardMBean;

/**
 * <p>Represents JMX OSGi MBeans handler.
 * Storing information about holden MBean.</p>
 * 
 * @version $Rev: 896239 $ $Date: 2010-01-05 17:02:23 -0500 (Tue, 05 Jan 2010) $
 */
public interface MBeanHandler {

    /**
     * Gets MBean holden by handler.
     * @return MBean @see {@link StandardMBean}.
     */
    StandardMBean getMbean();

    /**
     * Starts handler.
     */
    void open();

    /**
     * Stops handler.
     */
    void close();

    /**
     * Gets name of the MBean.
     * @return MBean name.
     */
    String getName();

}