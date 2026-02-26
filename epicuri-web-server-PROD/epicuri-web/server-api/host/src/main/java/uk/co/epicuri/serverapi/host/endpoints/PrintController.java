package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdList;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Print", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class PrintController {

    @Value("${epicuri.waiter.print.window}")
    private long printSpoolWindow;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private BatchService batchService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<?> putBatch(@NotNull @RequestBody PrintBatchRequest request) {
        batchService.markAsPrinted(request.getBatchId());
        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<HostBatchView>> getBatches(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<HostBatchView> batches = batchService.getHostBatchViews(restaurantId);
        //each batch is now spooled
        long time = System.currentTimeMillis();
        batches.forEach(b -> b.setSpoolTime(time/1000));
        liveDataService.addSpooledTime(batches.stream().map(HostBatchView::getId).collect(Collectors.toList()), time);

        return ResponseEntity.ok(batches);
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteBatches(@RequestHeader(Params.AUTHORIZATION) String token,
                                           @RequestBody IdList idList) {
        liveDataService.markBatchesDeleted(idList.getIds());
        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(path = "/all", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAllBatches(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Session> sessions = sessionService.getLiveSessions(restaurantId).stream().filter(s -> s.getSessionType() != SessionType.TAKEAWAY).collect(Collectors.toList());
        Map<String, List<Batch>> batches = liveDataService.getBatchesBySessionId(sessions.stream().map(Session::getId).collect(Collectors.toList()));
        List<Batch> batchesToDelete = batches.values().stream().flatMap(List::stream).collect(Collectors.toList());
        liveDataService.markBatchesDeleted(batchesToDelete.stream().map(Batch::getId).collect(Collectors.toList()));

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(path = "/spool", method = RequestMethod.PUT)
    public ResponseEntity<?> putSpoolBatches(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @RequestBody IdList idList) {
        liveDataService.flagForBatchPrinting(idList.getIds(), System.currentTimeMillis());
        return ResponseEntity.ok().build();
    }
}
