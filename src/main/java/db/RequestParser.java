package db;

import db.domain.User;
import db.utils.Tools;

public final class RequestParser {

    /** Utility class */
    private RequestParser() {}

    /**
     * [user | message | chat]:[id | system]:[methodName]:[arg,[...args]]
     * [user]:respond:[id]:[response]
     *
     * @param message request
     * @return response
     */
    public static String parse(final String message) {
        if (message == null || message.isBlank()) {
            throw new ServerException("Unknown command");
        }

        final String[] parts = message.split(":");

        if (parts.length < 3) {
            throw new ServerException("Expected at least 3 arguments for 'user | message | chat' command");
        }

        final String runOn = parts[0];

        switch (parts[1]) {
            case "system" -> {
                final String methodName = parts[2];

                final String[] args = parts.length == 3 ? new String[]{} : parts[3].split(",");

                final User user = getUser(Tools.SYSTEM_USER_ID);

                return GlobalObject.i().runMethod(user, runOn, methodName, (Object[]) args);
            }
            case "respond" -> {
                if (parts.length < 4) {
                    throw new ServerException("Invalid response from user: " + parts[2]);
                }

                final User user = getUser(parts[2]);
                return Validator.userResponse(user, parts[3]);
            }
            default -> {
                final User user = getUser(parts[1]);
                final String methodName = parts[2];

                final String[] args = parts.length == 3 ? new String[]{} : parts[3].split(",");

                return GlobalObject.i().runMethod(user, runOn, methodName, (Object[]) args);
            }
        }
    }

    private static User getUser(final String input) {
        try {
            final long id = Long.parseLong(input);

            return GlobalObject.i().getUserService().findUserById(id);
        } catch (final NumberFormatException e) {
            throw new ServerException("Expected 'id', but was: " + input, e);
        }
    }
}
