package se.hse.ticketservice.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import se.hse.ticketservice.dto.CreateOrderDto
import se.hse.ticketservice.service.OrderService
import se.hse.ticketservice.util.AuthenticationAspect
import se.hse.ticketservice.util.LoginRequired

@RestController
@RequestMapping("/ticket/orders")
class TicketOrderController(private val orderService: OrderService) {

    @PostMapping
    @LoginRequired
    fun createOrder(@Valid @RequestBody order: CreateOrderDto): ResponseEntity<Any> {
        val userId = AuthenticationAspect.getUserId()
            ?: return ResponseEntity.status(401).body("Unauthorized")
        return orderService.createOrder(order, userId)
    }

    @GetMapping("/{orderId}")
    @LoginRequired
    fun getOrderById(@PathVariable orderId: Long): ResponseEntity<Any> {
        val userId = AuthenticationAspect.getUserId()
            ?: return ResponseEntity.status(401).body("Unauthorized")
        return orderService.getOrderById(orderId, userId)
    }
}