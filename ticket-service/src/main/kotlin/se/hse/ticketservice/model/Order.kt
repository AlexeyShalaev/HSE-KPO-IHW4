package se.hse.ticketservice.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    val fromStationId: Long,
    val toStationId: Long,
    var status: Int = 1, // 1: check, 2: success, 3: rejection
    val created: Date = Date(),
)
