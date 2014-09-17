package org.jboss.gravia.container.tomcat.extension;

import java.security.Principal;

final class UserPrincipal implements Principal {

    private final String name;

    UserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}