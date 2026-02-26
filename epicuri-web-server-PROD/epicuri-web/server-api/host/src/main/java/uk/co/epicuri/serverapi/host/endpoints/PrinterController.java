package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.HostPrinterView;
import uk.co.epicuri.serverapi.common.pojo.host.HostPrinterViewUpdate;
import uk.co.epicuri.serverapi.common.pojo.host.PrinterRedirectRequest;
import uk.co.epicuri.serverapi.common.pojo.host.PrinterRedirectResponse;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.host.util.PrinterUtil;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Printer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class PrinterController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    public PrinterController() {

    }

    @CrossOrigin
    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<HostPrinterView>> getPrinters(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Printer> printers = masterDataService.getPrinters(restaurantId);

        return ResponseEntity.ok(printers.stream().map(HostPrinterView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putPrinterInfo(@RequestHeader(Params.AUTHORIZATION) String token,
                                           @RequestBody HostPrinterViewUpdate printerViewUpdate,
                                           @PathVariable("id") String printerId) {
        Printer printer = masterDataService.getPrinter(printerId);
        if(printer == null || !printer.getRestaurantId().equals(authenticationService.getRestaurantId(token))) {
            return ResponseEntity.notFound().build();
        }

        //don't allow blanks on IP address
        if(StringUtils.isBlank(printerViewUpdate.getIpAddress())) {
            return ResponseEntity.ok().build();
        }

        printer.setIp(printerViewUpdate.getIpAddress());
        printer.setMacAddress(printerViewUpdate.getMacAddress());
        masterDataService.upsert(printer);
        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Redirect", method = RequestMethod.PUT)
    public ResponseEntity<?> putRedirect(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @NotNull @RequestBody PrinterRedirectRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);
        //check model
        if(StringUtils.isBlank(request.getFrom()) || StringUtils.isBlank(request.getTo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if(request.getFrom().equals(request.getTo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //ensure both printers belong to restaurant
        Map<String,Printer> all = masterDataService.getPrinters(restaurantId).stream().collect(Collectors.toMap(Printer::getId, Function.identity()));
        if(!all.containsKey(request.getFrom()) || !all.containsKey(request.getTo())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Printer not found");
        }

        Printer from = all.get(request.getFrom());
        Printer to = all.get(request.getTo());
        if(PrinterUtil.isCyclic(from, to, all)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Redirect will cause a loop");
        }

        from.setRedirect(PrinterUtil.redirectId(from,to));
        masterDataService.upsert(from);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/RedirectedPrinters", method = RequestMethod.GET)
    public ResponseEntity<?> getRedirects(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Printer> allPrinters = masterDataService.getPrinters(restaurantId);
        List<Printer> printers = allPrinters.stream().filter(p -> StringUtils.isNotBlank(p.getRedirect())).collect(Collectors.toList());
        Map<String,Printer> printerMap = allPrinters.stream().collect(Collectors.toMap(Printer::getId, Function.identity()));

        List<PrinterRedirectResponse> response = new ArrayList<>();
        for(Printer from : printers) {
            if(StringUtils.isNotBlank(from.getRedirect()) && printerMap.containsKey(PrinterUtil.getRedirectId(from.getRedirect()))) {
                Printer to = printerMap.get(PrinterUtil.getRedirectId(from.getRedirect()));
                response.add(new PrinterRedirectResponse(from, to));
            }
        }

        return ResponseEntity.ok(response);
    }

    @HostAuthRequired
    @RequestMapping(value = "/Redirect/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteRedirect(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Printer> printers = masterDataService.getPrinters(restaurantId);

        for(Printer printer : printers) {
            if(id.equals(printer.getRedirect())) {
                printer.setRedirect(null);
                masterDataService.upsert(printer);
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.ok().build();
    }
}
