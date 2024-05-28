package se.hse.ticketservice.util

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginRequired

@Aspect
@Component
class AuthenticationAspect(private val jwtUtil: JwtUtil) {

    companion object {
        private val userIdThreadLocal = ThreadLocal<Long?>()

        fun getUserId(): Long? {
            return userIdThreadLocal.get()
        }

        private fun setUserId(userId: Long?) {
            userIdThreadLocal.set(userId)
        }

        private fun clearUserId() {
            userIdThreadLocal.remove()
        }
    }

    @Before("@annotation(LoginRequired)")
    fun checkAuthentication() {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val token = extractToken(request)

        if (token == null || !jwtUtil.validateToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized: No valid token provided")
        }

        val userId = token.let { jwtUtil.getUserIdFromToken(it) }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized: No valid token provided")

        setUserId(userId)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val headerToken = request.getHeader("Authorization")?.replace("Bearer ", "")
        val cookieToken = request.cookies?.firstOrNull { it.name == "token" }?.value
        val bodyToken = request.getParameter("token")

        return headerToken ?: cookieToken ?: bodyToken
    }
}
