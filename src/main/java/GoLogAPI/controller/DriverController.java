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
    public DriverResponse save(@Valid @RequestBody DriverCreateRequest driverCreateRequest) {
        return driverService.save(driverCreateRequest);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DriverResponse> get(@PathVariable UUID id){
        DriverResponse driverResponse = driverService.get(id);
        return ResponseEntity.ok(driverResponse);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id){
        driverService.delete(id);
    }

    @PutMapping("{id}")
    public DriverResponse update(@PathVariable UUID id, @Valid @RequestBody DriverCreateRequest driverCreateRequest){
        return driverService.update(id, driverCreateRequest);
    }


    @PatchMapping("{id}")
    public DriverResponse updatePartial(@PathVariable UUID id, @Valid @RequestBody DriverPatchRequest driverPatchRequest){
        return driverService.updatePartial(id, driverPatchRequest);
    }
}
