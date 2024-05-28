package se.hse.authservice.dto

data class LoginDto(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val detail: String
)