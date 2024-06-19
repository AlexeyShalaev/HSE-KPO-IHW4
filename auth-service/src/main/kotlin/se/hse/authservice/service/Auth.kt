package se.hse.authservice.service

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import se.hse.authservice.dto.AuthResponse
import se.hse.authservice.dto.LoginDto
import se.hse.authservice.dto.UserDto
import se.hse.authservice.exception.UserRegistrationException
import se.hse.authservice.model.Session
import se.hse.authservice.model.User
import se.hse.authservice.repository.SessionRepository
import se.hse.authservice.repository.UserRepository
import se.hse.authservice.util.JwtUtil
import java.util.regex.Pattern

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    // Добавим паттерны для проверки email и пароля
    val emailPattern: Pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    //val passwordPattern: Pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$") // PRODUCTION
    val passwordPattern: Pattern = Pattern.compile(".{4,}") // DEVELOPMENT

    fun registerUser(userDto: UserDto): ResponseEntity<AuthResponse> {
        // Проверка непустоты полей nickname, email и password
        if (userDto.nickname.isBlank() || userDto.email.isBlank() || userDto.password.isBlank()) {
            throw UserRegistrationException("Поля nickname, email и password не могут быть пустыми")
        }

        // Проверка корректности email с использованием регулярного выражения
        if (!emailPattern.matcher(userDto.email).matches()) {
            throw UserRegistrationException("Некорректный формат email")
        }

        // Проверка корректности пароля с использованием регулярного выражения
        if (!passwordPattern.matcher(userDto.password).matches()) {
            throw UserRegistrationException("Пароль должен состоять из не менее восьми символов, включая буквы обоих регистров, цифры и специальные символы")
        }

        // Проверка уникальности nickname

        if (userRepository.findByNickname(userDto.nickname) != null) {
            throw UserRegistrationException("Пользователь с таким nickname уже существует")
        }

        // Проверка уникальности email
        if (userRepository.findByEmail(userDto.email) != null) {
            throw UserRegistrationException("Пользователь с таким email уже существует")
        }

        // Создание пользователя
        val user = User(
            nickname = userDto.nickname,
            email = userDto.email,
            password = passwordEncoder.encode(userDto.password)
        )

        val insertedUser = userRepository.save(user)

        return authUser(insertedUser.id, HttpStatus.CREATED)
    }

    fun login(loginDto: LoginDto): ResponseEntity<AuthResponse> {
        val user = userRepository.findByEmail(loginDto.email)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse("", "Неверный email или пароль"))

        if (!passwordEncoder.matches(loginDto.password, user.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse("", "Неверный email или пароль"))
        }

        return authUser(user.id)
    }

    fun me(token: String): ResponseEntity<String> {
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный или просроченный токен")
        }

        sessionRepository.findByToken(token)

        val session = sessionRepository.findByToken(token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Токен не найден")

        if (session.expires.before(java.util.Date())) {
            sessionRepository.delete(session)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Токен просрочен")
        }

        val userId = jwtUtil.getUserIdFromToken(token)
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Пользователь не найден") }

        return ResponseEntity.ok("Информация о пользователе: ${user.nickname}, ${user.email}")
    }

    fun authUser(userId: Long, httpStatus: HttpStatus = HttpStatus.OK): ResponseEntity<AuthResponse> {
        val (token, expDate) = jwtUtil.generateToken(userId)

        val session = Session(
            userId = userId,
            token = token,
            expires = expDate
        )
        sessionRepository.save(session)

        val responseCookie = ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(expDate.time - System.currentTimeMillis())
            .build()

        val authResponse = AuthResponse(token = token, detail = "Вход выполнен успешно")

        return ResponseEntity.status(httpStatus)
            .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
            .body(authResponse)
    }
}
