package se.hse.ticketservice.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import se.hse.ticketservice.repository.StationRepository

@Service
class StationService(
    private val stationRepository: StationRepository,
) {

    fun getAllStations(): ResponseEntity<Any> {
        val stations = stationRepository.findAll()
        return ResponseEntity.ok(stations)
    }

}
