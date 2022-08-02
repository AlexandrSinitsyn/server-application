@file:JvmName("Validator")

package db

import db.annotations.Access
import db.annotations.Admin
import db.annotations.Confirmation
import db.annotations.PrivateOnly
import db.annotations.SystemOnly
import db.domain.User
import db.utils.Tools
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

private fun getAccessAnnotations(method: Method): Set<KClass<out Annotation>> =
    method.declaredAnnotations.filter { a -> a.annotationClass.hasAnnotation<Access>() }.map { a -> a::class }.toSet()

private data class ResponseWrapper(val test: (String) -> Boolean, val run: () -> String, var failed: Boolean)

private val awaitResponding = mutableMapOf<User, ResponseWrapper>()

fun userResponse(user: User, response: String): String {
    val confirmation = awaitResponding[user] ?: throw ServerException("Unexpected user response [$response] from $user")

    return if (confirmation.test(response)) {
        awaitResponding.remove(user)

        confirmation.run()
    } else {
        resendConfirmation(confirmation)
    }
}

fun canAccess(user: User, service: Any, method: Method, args: Array<Any>): Pair<Boolean, String> {
    val access = getAccessAnnotations(method)

    return when {
        access.contains(SystemOnly::class) -> (user.id == Tools.SYSTEM_USER_ID.toLong()) to "no access"
        access.contains(Admin::class) -> user.isAdmin to "no access"
        access.contains(PrivateOnly::class) -> true to "no access" // fixme user == GlobalObject.mainUser
        access.contains(Confirmation::class) -> false to sendConfirmation(user, service, method, args)
        else -> true to "no access"
    }
}

private fun resendConfirmation(confirmation: ResponseWrapper): String {
    confirmation.failed = true

    return "invalid"
}

private fun sendConfirmation(user: User, service: Any, method: Method, args: Array<Any>): String {
    return when (val confirmation = method.getDeclaredAnnotation(Confirmation::class.java).value) {
        "password" -> {
            awaitResponding[user] = ResponseWrapper({ resp -> GlobalObject.i().checkPassword(user, resp) },
                { GlobalObject.i().runMethod(service, method, args) }, false)

            "password"
        }
        "action" -> {
            "confirm"
        }
        else -> throw IllegalArgumentException("Unsupported confirmation: $confirmation")
    }
}
