package se.hse.authservice

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import se.hse.authservice.dto.LoginDto
import se.hse.authservice.dto.UserDto

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @Transactional
    fun `test register user successfully`() {
        val userDto = UserDto(nickname = "testuser", email = "test@example.com", password = "Password@123")
        val userJson = objectMapper.writeValueAsString(userDto)

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
        )
            .andExpect(status().isCreated)
            .andExpect { result ->
                val token = result.response.cookies.find { it.name == "token" }?.value
                Assertions.assertNotNull(token) // Проверяем, что токен также содержится в куке
                Assertions.assertNotNull(result.response.contentAsString.contains("token"))
            }
    }

    @Test
    @Transactional
    fun `test login successfully`() {
        // First, register a user
        val userDto = UserDto(nickname = "testuser", email = "test@example.com", password = "Password@123")
        val userJson = objectMapper.writeValueAsString(userDto)

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
        )
            .andExpect(status().isCreated)

        // Now, login with the same user
        val loginDto = LoginDto(email = "test@example.com", password = "Password@123")
        val loginJson = objectMapper.writeValueAsString(loginDto)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        )
            .andExpect(status().isOk)
            .andExpect(cookie().exists("token"))
            .andExpect { result ->
                val token = result.response.cookies.find { it.name == "token" }?.value
                Assertions.assertNotNull(token) // Проверяем, что токен также содержится в куке
                Assertions.assertNotNull(result.response.contentAsString.contains("token"))
                Assertions.assertNotNull(result.response.contentAsString.contains("Вход выполнен успешно"))
            }

    }

    @Test
    @Transactional
    fun `test me endpoint with header token`() {
        // First, register and login a user to get a token
        val userDto = UserDto(nickname = "testuser", email = "test@example.com", password = "Password@123")
        val userJson = objectMapper.writeValueAsString(userDto)

        val registerResult = mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val token = objectMapper.readTree(registerResult.response.contentAsString).get("token").asText()

        // Now, access the /me endpoint with the token in the header
        mockMvc.perform(
            get("/auth/me")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Информация о пользователе: testuser, test@example.com"))
    }

    @Test
    @Transactional
    fun `test me endpoint with cookie token`() {
        // First, register and login a user to get a token
        val userDto = UserDto(nickname = "testuser", email = "test@example.com", password = "Password@123")
        val userJson = objectMapper.writeValueAsString(userDto)

        val registerResult = mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val token = objectMapper.readTree(registerResult.response.contentAsString).get("token").asText()

        // Now, access the /me endpoint with the token in the cookie
        mockMvc.perform(
            get("/auth/me")
                .cookie(Cookie("token", token))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Информация о пользователе: testuser, test@example.com"))
    }

    @Test
    @Transactional
    fun `test me endpoint with body token`() {
        // First, register and login a user to get a token
        val userDto = UserDto(nickname = "testuser", email = "test@example.com", password = "Password@123")
        val userJson = objectMapper.writeValueAsString(userDto)

        val registerResult = mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val token = objectMapper.readTree(registerResult.response.contentAsString).get("token").asText()

        // Now, access the /me endpoint with the token in the body
        val body = mapOf("token" to token)
        val bodyJson = objectMapper.writeValueAsString(body)

        mockMvc.perform(
            get("/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Информация о пользователе: testuser, test@example.com"))
    }
}
