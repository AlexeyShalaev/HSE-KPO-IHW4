package se.hse.authservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import se.hse.authservice.dto.AuthResponse
import se.hse.authservice.dto.LoginDto
import se.hse.authservice.dto.UserDto
import se.hse.authservice.service.AuthService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun registerUser(@RequestBody userDto: UserDto): ResponseEntity<AuthResponse> {
        return authService.registerUser(userDto)
    }

    @PostMapping("/login")
    fun login(@RequestBody loginDto: LoginDto): ResponseEntity<AuthResponse> {
        return authService.login(loginDto)
    }

    @GetMapping("/me")
    fun me(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @CookieValue("token", required = false) tokenCookie: String?,
        @RequestBody(required = false) body: Map<String, String>?
    ): ResponseEntity<String> {
        val token = authHeader?.removePrefix("Bearer ")
            ?: body?.get("token")
            ?: tokenCookie
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Токен не предоставлен")

        return authService.me(token)
    }
}
