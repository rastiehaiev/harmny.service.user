package io.harmny.service.user.endpoints

import io.harmny.service.user.response.ValidityResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/validation"])
class ValidationEndpoint {

    @RequestMapping
    fun validate() = ResponseEntity.ok(ValidityResponse(true))
}
