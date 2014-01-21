/*
 * #%L
 * Gravia :: Container :: Tomcat :: Extension
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
package org.jboss.gravia.container.tomcat.extension;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.users.MemoryUserDatabase;

public class UserDatabaseLoginModule implements LoginModule {

    private UserDatabase userDatabase;
    private CallbackHandler callbackHandler;
    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;
    private Subject subject;
    private String login;
    private List<String> userGroups;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.callbackHandler = callbackHandler;
        this.subject = subject;
        try {
            userDatabase = new MemoryUserDatabase();
            userDatabase.open();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot open user database", ex);
        }
    }

    @Override
    public boolean login() throws LoginException {

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login");
        callbacks[1] = new PasswordCallback("password", true);

        userGroups = new ArrayList<String>();
        try {
            callbackHandler.handle(callbacks);
            String username = ((NameCallback) callbacks[0]).getName();
            String password = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());

            // Here we validate the credentials against some
            // authentication/authorization provider.

            User user = userDatabase.findUser(username);
            if (user != null && user.getPassword().equals(password)) {

                // We store the username and roles
                // fetched from the credentials provider
                // to be used later in commit() method.

                login = username;
                Iterator<Role> roles = user.getRoles();
                while(roles.hasNext()) {
                    Role role = roles.next();
                    userGroups.add(role.getName());
                }

                // Login success
                return true;

            } else {
                throw new LoginException("Authentication failed");
            }
        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (login == null || userGroups.isEmpty()) {
            return false;
        }
        userPrincipal = new UserPrincipal(login);
        subject.getPrincipals().add(userPrincipal);
        for (String groupName : userGroups) {
            rolePrincipal = new RolePrincipal(groupName);
            subject.getPrincipals().add(rolePrincipal);
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        login = null;
        userPrincipal = null;
        rolePrincipal = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(rolePrincipal);
        return true;
    }

    private static class UserPrincipal implements Principal {

        private final String name;

        public UserPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    private static class RolePrincipal implements Principal {

        private final String name;

        public RolePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
