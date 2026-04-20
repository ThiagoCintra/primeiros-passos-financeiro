package com.primeirospassos.financeiro.security;

import java.util.Optional;

public final class UserContextHolder {

    private static final ThreadLocal<CurrentUser> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(CurrentUser user) {
        CONTEXT.set(user);
    }

    public static Optional<CurrentUser> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
