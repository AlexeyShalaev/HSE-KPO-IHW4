package se.hse.ticketservice.repository

import se.hse.ticketservice.model.Station
import org.springframework.data.jpa.repository.JpaRepository

interface StationRepository : JpaRepository<Station, Long> {

}
