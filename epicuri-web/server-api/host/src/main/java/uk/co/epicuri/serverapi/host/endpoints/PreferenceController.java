package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.customer.KeyValuePair;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/User", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class PreferenceController {

    @Autowired
    private MasterDataService masterDataService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getPreferences() {
        Map<String, List<KeyValuePair>> response = masterDataService.getAllPreferencesAsMap();

        return ResponseEntity.ok(response);
    }
}
