package se.hse.ticketservice.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import se.hse.ticketservice.service.StationService


@RestController
@RequestMapping("/station")
class StationController(private val stationService: StationService) {

    @GetMapping("/all")
    fun getAllStations(): ResponseEntity<Any> {
        return stationService.getAllStations()
    }
}