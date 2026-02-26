package uk.co.epicuri.serverapi.client.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * Created by manish
 */
@RestController
@RequestMapping("/")
public class RootResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootResource.class);

    @RequestMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getRobots() {

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("robots.txt");
        if(inputStream != null) {
            LOGGER.trace("Robots txt requested. ");
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
            return new ResponseEntity<>(inputStreamResource, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/ping", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE}, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> ping() {
        return new ResponseEntity<>("Pong", HttpStatus.OK);
    }

}
