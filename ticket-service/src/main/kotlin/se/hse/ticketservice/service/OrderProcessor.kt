package se.hse.ticketservice.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random


@Component
class OrderHandlerEmulator @Autowired constructor(private val orderService: OrderService) {

    @Scheduled(fixedRate = 5000)
    fun processOrders() {
        val ordersToProcess = orderService.getOrdersByStatus(1) // Status 1: check
        for (order in ordersToProcess) {
            order.status = if (Random.nextBoolean()) 2 else 3 // 2: success, 3: rejection
            orderService.updateOrder(order)
        }
    }
}
