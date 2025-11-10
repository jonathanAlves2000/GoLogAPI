package GoLogAPI.controller;

import GoLogAPI.model.Driver;
import GoLogAPI.service.DriverService;
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
    public Driver SaveDriver(@RequestBody Driver driver) {
        return this.driverService.saveDriver(driver);
    }

    @GetMapping("{id}")
    public Driver DisplayDriver(@PathVariable int id){
        return this.driverService.displayDriver(id);
    }

    @DeleteMapping("{id}")
    public void DeleteDriver(@PathVariable int id){
        this.driverService.deleteDriver(id);
    }

    @PutMapping("{id}")
    public Driver UpdateDriver(@PathVariable int id, @RequestBody Driver driver){
        return this.driverService.saveDriver(driver);
    }

    @GetMapping
    public List<Driver> SearchDriver(@RequestParam String name){
        return this.driverService.searchDriver(name);
    }
}
