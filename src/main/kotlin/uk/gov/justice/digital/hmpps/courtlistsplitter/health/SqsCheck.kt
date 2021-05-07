package uk.gov.justice.digital.hmpps.courtlistsplitter.health

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.SqsService

@Component
class SqsCheck(@Autowired private val sqsService: SqsService) : HealthIndicator {

  override fun health(): Health {
    if (sqsService.isQueueAvailable()) {
      return Health.up().build()
    }
    return Health.down().build()
  }
}
