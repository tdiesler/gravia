/*
 * Copyright (c) OSGi Alliance (2007, 2012). All Rights Reserved.
 *
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
 */

package org.jboss.gravia.runtime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.gravia.runtime.Module.State;

/**
 * The {@code ModuleTracker} class simplifies tracking modules much like the
 * {@code ServiceTracker} simplifies tracking services.
 * <p>
 * A {@code ModuleTracker} is constructed with state criteria and a
 * {@code ModuleTrackerCustomizer} object. A {@code ModuleTracker} can use the
 * {@code ModuleTrackerCustomizer} to select which modules are tracked and to
 * create a customized object to be tracked with the module. The
 * {@code ModuleTracker} can then be opened to begin tracking all modules whose
 * state matches the specified state criteria.
 * <p>
 * The {@code getModules} method can be called to get the {@code Module} objects
 * of the modules being tracked. The {@code getObject} method can be called to
 * get the customized object for a tracked module.
 * <p>
 * The {@code ModuleTracker} class is thread-safe. It does not call a
 * {@code ModuleTrackerCustomizer} while holding any locks.
 * {@code ModuleTrackerCustomizer} implementations must also be thread-safe.
 *
 * @param <T> The type of the tracked object.
 * @ThreadSafe
 */
public class ModuleTracker<T> implements ModuleTrackerCustomizer<T> {
    /* set this to true to compile in debug messages */
    static final boolean DEBUG = false;

    /**
     * The Module Context used by this {@code ModuleTracker}.
     */
    protected final ModuleContext context;

    /**
     * The {@code ModuleTrackerCustomizer} object for this tracker.
     */
    final ModuleTrackerCustomizer<T> customizer;

    /**
     * Tracked modules: {@code Module} object -> customized Object and
     * {@code ModuleListener} object
     */
    private volatile Tracked tracked;

    /**
     * Accessor method for the current Tracked object. This method is only
     * intended to be used by the unsynchronized methods which do not modify the
     * tracked field.
     *
     * @return The current Tracked object.
     */
    private Tracked tracked() {
        return tracked;
    }

    /**
     * State mask for modules being tracked. This field contains the set
     * of the module states being tracked.
     */
    final Set<State> states = new HashSet<State>();

    /**
     * Create a {@code ModuleTracker} for modules whose state is present in the
     * specified state mask.
     *
     * <p>
     * Modules whose state is present on the specified state mask will be
     * tracked by this {@code ModuleTracker}.
     *
     * @param context The {@code ModuleContext} against which the tracking is
     *        done.
     * @param states The list of the module states to be tracked.
     * @param customizer The customizer object to call when modules are added,
     *        modified, or removed in this {@code ModuleTracker}. If customizer
     *        is {@code null}, then this {@code ModuleTracker} will be used as
     *        the {@code ModuleTrackerCustomizer} and this {@code ModuleTracker}
     *        will call the {@code ModuleTrackerCustomizer} methods on itself.
     * @see Module#getState()
     */
    public ModuleTracker(ModuleContext context, ModuleTrackerCustomizer<T> customizer, State... states) {
        this.context = context;
        this.customizer = (customizer == null) ? this : customizer;
        if (states != null) {
            this.states.addAll(Arrays.asList(states));
        }
    }

    /**
     * Open this {@code ModuleTracker} and begin tracking modules.
     *
     * <p>
     * Module which match the state criteria specified when this
     * {@code ModuleTracker} was created are now tracked by this
     * {@code ModuleTracker}.
     *
     * @throws java.lang.IllegalStateException If the {@code ModuleContext} with
     *         which this {@code ModuleTracker} was created is no longer valid.
     * @throws java.lang.SecurityException If the caller and this class do not
     *         have the appropriate
     *         {@code AdminPermission[context module,LISTENER]}, and the Java
     *         Runtime Environment supports permissions.
     */
    public void open() {
        final Tracked t;
        synchronized (this) {
            if (tracked != null) {
                return;
            }
            if (DEBUG) {
                System.out.println("ModuleTracker.open");
            }
            t = new Tracked();
            synchronized (t) {
                context.addModuleListener(t);
                Runtime runtime = RuntimeLocator.getRequiredRuntime();
                Set<Module> modules = runtime.getModules();
                Iterator<Module> itmods = modules.iterator();
                while (itmods.hasNext()) {
                    Module module = itmods.next();
                    State state = module.getState();
                    if (!states.contains(state)) {
                        itmods.remove();
                    }
                }
                /* set tracked with the initial modules */
                t.setInitial((Module[]) modules.toArray());
            }
            tracked = t;
        }
        /* Call tracked outside of synchronized region */
        t.trackInitial(); /* process the initial references */
    }

    /**
     * Close this {@code ModuleTracker}.
     *
     * <p>
     * This method should be called when this {@code ModuleTracker} should end
     * the tracking of modules.
     *
     * <p>
     * This implementation calls {@link #getModules()} to get the list of
     * tracked modules to remove.
     */
    public void close() {
        final Module[] modules;
        final Tracked outgoing;
        synchronized (this) {
            outgoing = tracked;
            if (outgoing == null) {
                return;
            }
            if (DEBUG) {
                System.out.println("ModuleTracker.close");
            }
            outgoing.close();
            modules = getModules();
            tracked = null;
            try {
                context.removeModuleListener(outgoing);
            } catch (IllegalStateException e) {
                /* In case the context was stopped. */
            }
        }
        if (modules != null) {
            for (int i = 0; i < modules.length; i++) {
                outgoing.untrack(modules[i], null);
            }
        }
    }

    /**
     * Default implementation of the
     * {@code ModuleTrackerCustomizer.addingModule} method.
     *
     * <p>
     * This method is only called when this {@code ModuleTracker} has been
     * constructed with a {@code null ModuleTrackerCustomizer} argument.
     *
     * <p>
     * This implementation simply returns the specified {@code Module}.
     *
     * <p>
     * This method can be overridden in a subclass to customize the object to be
     * tracked for the module being added.
     *
     * @param module The {@code Module} being added to this
     *        {@code ModuleTracker} object.
     * @param event The module event which caused this customizer method to be
     *        called or {@code null} if there is no module event associated with
     *        the call to this method.
     * @return The specified module.
     * @see ModuleTrackerCustomizer#addingModule(Module, ModuleEvent)
     */
    @SuppressWarnings("unchecked")
    public T addingModule(Module module, ModuleEvent event) {
        T result = (T) module;
        return result;
    }

    /**
     * Default implementation of the
     * {@code ModuleTrackerCustomizer.modifiedModule} method.
     *
     * <p>
     * This method is only called when this {@code ModuleTracker} has been
     * constructed with a {@code null ModuleTrackerCustomizer} argument.
     *
     * <p>
     * This implementation does nothing.
     *
     * @param module The {@code Module} whose state has been modified.
     * @param event The module event which caused this customizer method to be
     *        called or {@code null} if there is no module event associated with
     *        the call to this method.
     * @param object The customized object for the specified Module.
     * @see ModuleTrackerCustomizer#modifiedModule(Module, ModuleEvent, Object)
     */
    public void modifiedModule(Module module, ModuleEvent event, T object) {
        /* do nothing */
    }

    /**
     * Default implementation of the
     * {@code ModuleTrackerCustomizer.removedModule} method.
     *
     * <p>
     * This method is only called when this {@code ModuleTracker} has been
     * constructed with a {@code null ModuleTrackerCustomizer} argument.
     *
     * <p>
     * This implementation does nothing.
     *
     * @param module The {@code Module} being removed.
     * @param event The module event which caused this customizer method to be
     *        called or {@code null} if there is no module event associated with
     *        the call to this method.
     * @param object The customized object for the specified module.
     * @see ModuleTrackerCustomizer#removedModule(Module, ModuleEvent, Object)
     */
    public void removedModule(Module module, ModuleEvent event, T object) {
        /* do nothing */
    }

    /**
     * Return an array of {@code Module}s for all modules being tracked by this
     * {@code ModuleTracker}.
     *
     * @return An array of {@code Module}s or {@code null} if no modules are
     *         being tracked.
     */
    public Module[] getModules() {
        final Tracked t = tracked();
        if (t == null) { /* if ModuleTracker is not open */
            return null;
        }
        synchronized (t) {
            int length = t.size();
            if (length == 0) {
                return null;
            }
            return t.copyKeys(new Module[length]);
        }
    }

    /**
     * Returns the customized object for the specified {@code Module} if the
     * specified module is being tracked by this {@code ModuleTracker}.
     *
     * @param module The {@code Module} being tracked.
     * @return The customized object for the specified {@code Module} or
     *         {@code null} if the specified {@code Module} is not being
     *         tracked.
     */
    public T getObject(Module module) {
        final Tracked t = tracked();
        if (t == null) { /* if ModuleTracker is not open */
            return null;
        }
        synchronized (t) {
            return t.getCustomizedObject(module);
        }
    }

    /**
     * Remove a module from this {@code ModuleTracker}.
     *
     * The specified module will be removed from this {@code ModuleTracker} . If
     * the specified module was being tracked then the
     * {@code ModuleTrackerCustomizer.removedModule} method will be called for
     * that module.
     *
     * @param module The {@code Module} to be removed.
     */
    public void remove(Module module) {
        final Tracked t = tracked();
        if (t == null) { /* if ModuleTracker is not open */
            return;
        }
        t.untrack(module, null);
    }

    /**
     * Return the number of modules being tracked by this {@code ModuleTracker}.
     *
     * @return The number of modules being tracked.
     */
    public int size() {
        final Tracked t = tracked();
        if (t == null) { /* if ModuleTracker is not open */
            return 0;
        }
        synchronized (t) {
            return t.size();
        }
    }

    /**
     * Returns the tracking count for this {@code ModuleTracker}.
     *
     * The tracking count is initialized to 0 when this {@code ModuleTracker} is
     * opened. Every time a module is added, modified or removed from this
     * {@code ModuleTracker} the tracking count is incremented.
     *
     * <p>
     * The tracking count can be used to determine if this {@code ModuleTracker}
     * has added, modified or removed a module by comparing a tracking count
     * value previously collected with the current tracking count value. If the
     * value has not changed, then no module has been added, modified or removed
     * from this {@code ModuleTracker} since the previous tracking count was
     * collected.
     *
     * @return The tracking count for this {@code ModuleTracker} or -1 if this
     *         {@code ModuleTracker} is not open.
     */
    public int getTrackingCount() {
        final Tracked t = tracked();
        if (t == null) { /* if ModuleTracker is not open */
            return -1;
        }
        synchronized (t) {
            return t.getTrackingCount();
        }
    }

    /**
     * Return a {@code Map} with the {@code Module}s and customized objects for
     * all modules being tracked by this {@code ModuleTracker}.
     *
     * @return A {@code Map} with the {@code Module}s and customized objects for
     *         all services being tracked by this {@code ModuleTracker}. If no
     *         modules are being tracked, then the returned map is empty.
     */
    public Map<Module, T> getTracked() {
        Map<Module, T> map = new HashMap<Module, T>();
        final Tracked t = tracked();
        if (t == null) { /* if ModuleTracker is not open */
            return map;
        }
        synchronized (t) {
            return t.copyEntries(map);
        }
    }

    /**
     * Return if this {@code ModuleTracker} is empty.
     *
     * @return {@code true} if this {@code ModuleTracker} is not tracking any
     *         modules.
     */
    public boolean isEmpty() {
        final Tracked t = tracked();
        if (t == null) { /* if ModuleTracker is not open */
            return true;
        }
        synchronized (t) {
            return t.isEmpty();
        }
    }

    /**
     * Inner class which subclasses AbstractTracked. This class is the
     * {@code SynchronousModuleListener} object for the tracker.
     *
     * @ThreadSafe
     */
    private final class Tracked extends AbstractTracked<Module, T, ModuleEvent> implements SynchronousModuleListener {
        /**
         * Tracked constructor.
         */
        Tracked() {
            super();
        }

        /**
         * {@code ModuleListener} method for the {@code ModuleTracker} class.
         * This method must NOT be synchronized to avoid deadlock potential.
         *
         * @param event {@code ModuleEvent} object from the framework.
         */
        public void moduleChanged(final ModuleEvent event) {
            /*
             * Check if we had a delayed call (which could happen when we
             * close).
             */
            if (closed) {
                return;
            }
            final Module module = event.getModule();
            final State state = module.getState();
            if (DEBUG) {
                System.out.println("ModuleTracker.Tracked.moduleChanged[" + state + "]: " + module);
            }

            if (states.contains(state)) {
                track(module, event);
                /*
                 * If the customizer throws an unchecked exception, it is safe
                 * to let it propagate
                 */
            } else {
                untrack(module, event);
                /*
                 * If the customizer throws an unchecked exception, it is safe
                 * to let it propagate
                 */
            }
        }

        /**
         * Call the specific customizer adding method. This method must not be
         * called while synchronized on this object.
         *
         * @param item Item to be tracked.
         * @param related Action related object.
         * @return Customized object for the tracked item or {@code null} if the
         *         item is not to be tracked.
         */
        T customizerAdding(final Module item, final ModuleEvent related) {
            return customizer.addingModule(item, related);
        }

        /**
         * Call the specific customizer modified method. This method must not be
         * called while synchronized on this object.
         *
         * @param item Tracked item.
         * @param related Action related object.
         * @param object Customized object for the tracked item.
         */
        void customizerModified(final Module item, final ModuleEvent related, final T object) {
            customizer.modifiedModule(item, related, object);
        }

        /**
         * Call the specific customizer removed method. This method must not be
         * called while synchronized on this object.
         *
         * @param item Tracked item.
         * @param related Action related object.
         * @param object Customized object for the tracked item.
         */
        void customizerRemoved(final Module item, final ModuleEvent related, final T object) {
            customizer.removedModule(item, related, object);
        }
    }
}
