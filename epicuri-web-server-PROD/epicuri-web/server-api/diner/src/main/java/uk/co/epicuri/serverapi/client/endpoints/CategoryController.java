package uk.co.epicuri.serverapi.client.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.epicuri.serverapi.common.pojo.common.CuisineView;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.model.Cuisine;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Category", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

    @Autowired
    private MasterDataService masterDataDatabase;

    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getCuisineCategories() {
        List<Cuisine> cuisines = masterDataDatabase.getCuisines();
        return ResponseEntity.ok(cuisines.stream().map(CuisineView::new).collect(Collectors.toList()));
    }
}
