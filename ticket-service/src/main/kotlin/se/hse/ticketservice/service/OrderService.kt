package se.hse.ticketservice.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import se.hse.ticketservice.dto.CreateOrderDto
import se.hse.ticketservice.dto.CreatedOrderDto
import se.hse.ticketservice.exception.OrderException
import se.hse.ticketservice.model.Order
import se.hse.ticketservice.repository.OrderRepository
import se.hse.ticketservice.repository.StationRepository

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val stationRepository: StationRepository,
) {

    fun createOrder(order: CreateOrderDto, userId: Long): ResponseEntity<Any> {
        val fromStation = stationRepository.findById(order.fromStationId)
            .orElseThrow { OrderException("Станция отправления не найдена") }

        val toStation = stationRepository.findById(order.toStationId)
            .orElseThrow { OrderException("Станция прибытия не найдена") }

        if (fromStation.id == toStation.id) {
            throw OrderException("Станция отправления и прибытия не могут совпадать")
        }

        val newOrder = Order(
            userId = userId,
            fromStationId = fromStation.id,
            toStationId = toStation.id,
        )

        val insertedOrder = orderRepository.save(newOrder)

        return ResponseEntity.ok(
            CreatedOrderDto(
                detail = "Билет успешно создан",
                id = insertedOrder.id,
            )
        )
    }

    fun getOrderById(orderId: Long, userId: Long): ResponseEntity<Any> {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderException("Билет не найден") }

        if (order.userId != userId) {
            throw OrderException("Недостаточно прав")
        }

        return ResponseEntity.ok(order)
    }

    fun getOrdersByStatus(status: Int): List<Order> {
        return orderRepository.findByStatus(status)
    }

    fun updateOrder(order: Order): Order {
        return orderRepository.save(order)
    }

}
