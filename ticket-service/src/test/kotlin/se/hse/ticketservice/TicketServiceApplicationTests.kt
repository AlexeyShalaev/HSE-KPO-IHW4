package se.hse.ticketservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import se.hse.ticketservice.dto.CreateOrderDto
import se.hse.ticketservice.util.JwtUtil

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Test
    @Transactional
    fun `test get stations all`() {
        mockMvc.perform(
            get("/station/all")
        )
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    fun `test create order`() {
        val userId = 1L
        val token = jwtUtil.generateToken(userId)

        val createOrderDto = CreateOrderDto(fromStationId = 1L, toStationId = 2L)
        val createOrderJson = objectMapper.writeValueAsString(createOrderDto)

        mockMvc.perform(
            post("/ticket/orders")
                .header("Authorization", "Bearer $token") // Авторизация с использованием токена
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderJson)
        )
            .andExpect(status().isOk)
            .andExpect { result ->
                Assertions.assertNotNull(result.response.contentAsString.contains("id"))
                Assertions.assertNotNull(result.response.contentAsString.contains("Билет успешно создан"))
            }
    }

    @Test
    @Transactional
    fun `test get order`() {
        val userId = 1L
        val token = jwtUtil.generateToken(userId)

        val createOrderDto = CreateOrderDto(fromStationId = 1L, toStationId = 2L)
        val createOrderJson = objectMapper.writeValueAsString(createOrderDto)

        val orderResponse = mockMvc.perform(
            post("/ticket/orders")
                .header("Authorization", "Bearer $token") // Авторизация с использованием токена
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderJson)
        )
            .andExpect(status().isOk)
            .andReturn()

        val orderId = objectMapper.readTree(orderResponse.response.contentAsString).get("id").asText()

        mockMvc.perform(
            get("/ticket/orders/$orderId")
                .header("Authorization", "Bearer $token") // Авторизация с использованием токена
        )
            .andExpect(status().isOk)
    }

}
