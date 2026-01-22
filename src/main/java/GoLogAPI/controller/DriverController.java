package GoLogAPI.controller;

import GoLogAPI.dto.DriverDto;
import GoLogAPI.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("driver")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService){
        this.driverService = driverService;
    }

    @PostMapping
    public DriverDto SaveDriver(@RequestBody @Valid DriverDto driverDTO) {
        return this.driverService.saveDriver(driverDTO);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DriverDto> GetDriverById(@PathVariable int id){
        DriverDto driverDto = driverService.getDriverById(id);
        return ResponseEntity.ok(driverDto);
    }

    @DeleteMapping("{id}")
    public void DeleteDriver(@PathVariable int id){
        this.driverService.deleteDriver(id);
    }

    @PutMapping("{id}")
    public DriverDto UpdateDriver(@PathVariable int id, @RequestBody @Valid DriverDto driverDTO){
        return this.driverService.updateDriver(id, driverDTO);
    }
}
