# Server application
Basic server application to store users in database and provide access to them

Framework for managing database is Hibernate

This application provides an ability to get requests from the net and (if the user has specific access) do it.

Request parser accepts only 2 types of messages:
- `[user | message | chat]:[id | system]:[methodName]:[arg, [...args]]` - this application now can provide only DB for users, but it was to be for messages and chats as well.
- `[user]:respond:[id]:[response]` - response from user

Some methods in [UserService.java](https://github.com/AlexandrSinitsyn/server-application/blob/main/src/main/java/db/services/UserService.java) are marked this annotation `@Confirmation(value = ...)`. It means that server will not run this method until user confirms his action.

