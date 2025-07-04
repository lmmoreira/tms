package br.com.logistics.tms.commons.infrastructure.security;

public class UserContext {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(final User user) {
        currentUser.set(user);
    }

    public static void clear() {
        currentUser.remove();
    }

}