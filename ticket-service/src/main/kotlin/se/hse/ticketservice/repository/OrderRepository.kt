package se.hse.ticketservice.repository

import se.hse.ticketservice.model.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByStatus(status: Int): List<Order>
}
