package se.hse.ticketservice.dto

import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionDetails
import java.util.*

data class CreateOrderDto(
    val fromStationId: Long,
    val toStationId: Long,
)

data class CreatedOrderDto(
    val detail: String,
    val id: Long,
)