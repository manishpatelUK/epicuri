package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;

@CrossOrigin
@RestController
@RequestMapping(value = "/healthstatus", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CustomHealthCheckResource {

    @HostAuthRequired
    @RequestMapping(value = "/xping", method = RequestMethod.GET)
    public ResponseEntity<String> getXPing() {
        return ResponseEntity.ok("OK");
    }
}
