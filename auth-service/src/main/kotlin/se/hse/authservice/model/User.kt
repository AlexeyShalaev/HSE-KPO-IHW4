package se.hse.authservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val nickname: String,
    val email: String,
    val password: String,
    val created: LocalDateTime = LocalDateTime.now()
)
