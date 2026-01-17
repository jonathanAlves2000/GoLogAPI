package GoLogAPI.controller;

import GoLogAPI.dto.DriverDTO;
import GoLogAPI.model.Driver;
import GoLogAPI.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("driver")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService){
        this.driverService = driverService;
    }

    @PostMapping
    public Driver SaveDriver(@RequestBody @Valid DriverDTO driverDTO) {
        return this.driverService.saveDriver(driverDTO);
    }

    @GetMapping("{id}")
    public Driver GetDriverById(@PathVariable int id){
        return this.driverService.getDriverById(id);
    }

    @DeleteMapping("{id}")
    public void DeleteDriver(@PathVariable int id){
        this.driverService.deleteDriver(id);
    }

    @PutMapping("{id}")
    public Driver UpdateDriver(@PathVariable int id, @RequestBody @Valid DriverDTO driverDTO){
        return this.driverService.updateDriver(id, driverDTO);
    }
}
