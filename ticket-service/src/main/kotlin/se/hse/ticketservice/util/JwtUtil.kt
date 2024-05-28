package se.hse.ticketservice.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    fun generateToken(userId: Long, expInMs: Long = 8640000): String {
        val claims = Jwts.claims().setSubject(userId.toString())
        val now = Date()
        val validity = Date(now.time + expInMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            System.currentTimeMillis() < Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(token).body.expiration.time
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): Long? {
        return try {
            val claims: Claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
            claims.subject.toLong()
        } catch (e: Exception) {
            null
        }
    }
}
