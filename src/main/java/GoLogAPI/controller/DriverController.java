package GoLogAPI.controller;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverPatchRequest;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/driver")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService){
        this.driverService = driverService;
    }

    @PostMapping
    public DriverResponse SaveDriver(@Valid @RequestBody DriverCreateRequest driverCreateRequest) {
        return driverService.saveDriver(driverCreateRequest);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DriverResponse> GetDriver(@PathVariable UUID id){
        DriverResponse driverResponse = driverService.getDriver(id);
        return ResponseEntity.ok(driverResponse);
    }

    @DeleteMapping("{id}")
    public void DeleteDriver(@PathVariable UUID id){
        driverService.deleteDriver(id);
    }

    @PutMapping("{id}")
    public DriverResponse PutDriver(@PathVariable UUID id, @Valid @RequestBody DriverCreateRequest driverCreateRequest){
        return driverService.putDriver(id, driverCreateRequest);
    }


    @PatchMapping("{id}")
    public DriverResponse PatchDriver(@PathVariable UUID id, @Valid @RequestBody DriverPatchRequest driverPatchRequest){
        return driverService.patchDriver(id, driverPatchRequest);
    }
}
