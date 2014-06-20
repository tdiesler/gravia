/*
 * #%L
 * Gravia :: Arquillian :: Container
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
package org.jboss.gravia.arquillian.container;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.context.ObjectStore;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.gravia.arquillian.container.SetupTask.SetupContext;

/**
 * An Arquillian container setup observer.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 18-Jun-2014
 */
public class SetupObserver<T extends SetupTask> {

    @Inject
    private Instance<SuiteContext> suiteContextInstance;

    @Inject
    private Instance<ClassContext> classContextInstance;

    @SuppressWarnings("serial")
    private static class TaskList<T extends SetupTask> extends ArrayList<T> {}

    @SuppressWarnings("unchecked")
    public List<T> getSetupTasks() throws Exception {

        TaskList<T> taskList = null;

        // Get or create the task list with the {@link ClassContext}
        ClassContext classContext = classContextInstance.get();
        if (classContext.isActive()) {
            ObjectStore objectStore = classContext.getObjectStore();
            taskList = objectStore.get(TaskList.class);
            if (taskList == null) {
                taskList = new TaskList<T>();
                Class<?> currentClass = classContext.getActiveId();
                ContainerSetup setup = currentClass.getAnnotation(ContainerSetup.class);
                if (setup != null) {
                    Class<T>[] classes = (Class<T>[]) setup.value();
                    for (Class<T> clazz : classes) {
                        taskList.add(clazz.newInstance());
                    }
                }
                classContext.getObjectStore().add(TaskList.class, taskList);

                ObjectStore suiteStore = getSuiteObjectStore();
                suiteStore.get(TaskList.class).addAll(taskList);
            }
        }

        return taskList != null ? taskList : new TaskList<T>();
    }

    @SuppressWarnings("unchecked")
    protected <C extends SetupContext> C getSetupContext(ObjectStore suiteStore, ObjectStore classStore) {
        return (C) new AbstractSetupContext(suiteStore, classStore);
    }

    protected ObjectStore getSuiteObjectStore() {
        SuiteContext suiteContext = suiteContextInstance.get();
        return suiteContext.getObjectStore();
    }

    public void handleBeforeSuite(@Observes BeforeSuite event) throws Throwable {
        ObjectStore objectStore = getSuiteObjectStore();
        objectStore.add(TaskList.class, new TaskList<>());
    }

    public void handleBeforeClass(@Observes BeforeClass event) throws Throwable {
        List<T> setupTasks = getSetupTasks();
        if (!setupTasks.isEmpty()) {
            ClassContext classContext = classContextInstance.get();
            ObjectStore suiteStore = suiteContextInstance.get().getObjectStore();
            ObjectStore classStore = classContext.getObjectStore();
            SetupContext context = getSetupContext(suiteStore, classStore);
            for (T task : setupTasks) {
                task.beforeClass(context);
            }
        }
    }

    public void handleAfterClass(@Observes AfterClass event) throws Throwable {
        List<T> setupTasks = getSetupTasks();
        if (!setupTasks.isEmpty()) {
            ClassContext classContext = classContextInstance.get();
            ObjectStore classStore = classContext.getObjectStore();
            ObjectStore suiteStore = suiteContextInstance.get().getObjectStore();
            SetupContext context = getSetupContext(suiteStore, classStore);
            for (T task : setupTasks) {
                task.afterClass(context);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void handleAfterSuite(@Observes AfterSuite event) throws Throwable {
        SuiteContext suiteContext = suiteContextInstance.get();
        TaskList<T> setupTasks = suiteContext.getObjectStore().get(TaskList.class);
        if (!setupTasks.isEmpty()) {
            ObjectStore suiteStore = suiteContext.getObjectStore();
            SetupContext context = new AbstractSetupContext(suiteStore, null);
            for (T task : setupTasks) {
                task.afterSuite(context);
            }
        }
    }

    public static class AbstractSetupContext implements SetupContext {
        private final ObjectStore suiteStore;
        private final ObjectStore classStore;

        public AbstractSetupContext(ObjectStore suiteStore, ObjectStore classStore) {
            this.suiteStore = suiteStore;
            this.classStore = classStore;
        }

        @Override
        public ObjectStore getSuiteStore() {
            return suiteStore;
        }

        @Override
        public ObjectStore getClassStore() {
            return classStore;
        }
    }
}
