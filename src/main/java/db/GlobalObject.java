package db;

import db.domain.User;
import db.services.UserService;
import db.utils.Tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public final class GlobalObject {

    private static final GlobalObject instance = new GlobalObject();
    private final UserService userService = new UserService();
    // private final MessageService messageService = new MessageService();
    // private final ChatService chatService = new ChatService();

    /** Utility class */
    private GlobalObject() {}

    public static GlobalObject i() {
        return instance;
    }

    public UserService getUserService() {
        return userService;
    }

    public boolean checkPassword(final User user, final String password) {
        return userService.checkPassword(user, password);
    }

    public String runMethod(final User user, final String runOn, final String methodName, final Object... args) {
        final String className = Character.toUpperCase(runOn.charAt(0)) + runOn.substring(1) + "Service";
        final Object service = switch (runOn) {
            case "user" -> userService;
            // case "message" -> messageService;
            // case "chat" -> chatService;
            default -> throw new ServerException("Unknown command");
        };
        try {
            final Class<?> clazz = Class.forName(className);

            final Class<?>[] signature = (Class<?>[]) Arrays.stream(args).map(Object::getClass).toArray(Object[]::new);

            loop: for (final var method : clazz.getDeclaredMethods()) {
                if (!method.getName().equals(methodName) || signature.length != method.getParameterCount()) {
                    continue;
                }

                final Class<?>[] parameterTypes = method.getParameterTypes();

                for (int i = 0; i < signature.length; i++) {
                    if (!signature[i].equals(parameterTypes[i])) {
                        continue loop;
                    }
                }

                final var result = Validator.canAccess(user, service, method, args);
                if (!result.component1()) {
                    Tools.log("User '%s' has no access to do this action [%s]", user, methodName);

                    return result.component2();
                }

                return runMethod(service, method, args);
            }
        } catch (final ClassNotFoundException e) {
            throw new ServerException("Cannot find class " + className, e);
        }

        return null;
    }

    public String runMethod(final Object service, final Method method, final Object[] args) {
        try {
            if (method.getReturnType() == void.class) {
                method.invoke(service, args);

                return null;
            }

            return convertReturn(method.invoke(service, args));
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new ServerException("Cannot invoke method " + method.getName(), e);
        }
    }

    // fixme ???
    private String convertReturn(final Object result) {
        return Objects.toString(result);
    }
}
