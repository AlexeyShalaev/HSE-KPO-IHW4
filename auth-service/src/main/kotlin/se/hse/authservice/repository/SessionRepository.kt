package se.hse.authservice.repository

import se.hse.authservice.model.Session
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface SessionRepository : JpaRepository<Session, Long> {
    fun findByToken(token: String): Session?
}
