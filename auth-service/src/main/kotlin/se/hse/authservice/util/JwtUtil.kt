package se.hse.authservice.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration}")
    private var jwtExpirationInMs: Long = 0

    fun generateToken(userId: Long): Pair<String, Date> {
        val claims = Jwts.claims().setSubject(userId.toString())
        val now = Date()
        val validity = Date(now.time + jwtExpirationInMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact() to validity
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): Long {
        val claims: Claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
        return claims.subject.toLong()
    }
}
