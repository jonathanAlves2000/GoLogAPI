package SmartRouteAPI.service;

import SmartRouteAPI.model.Driver;
import SmartRouteAPI.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {

    private DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository){
        this.driverRepository = driverRepository;
    }

    public Driver saveDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public Driver displayDriver(int id){
        return driverRepository.findById(id).orElse(null);
    }

    public void deleteDriver(int id){
        driverRepository.deleteById(id);
    }

    public Driver updateDriver(int id, Driver driver){
          if(driverRepository.existsById(id)) {
              driver.setId(id);
              return driverRepository.save(driver);
          }
          return null;
    }

    public List<Driver> searchDriver(String name){
        return driverRepository.findByName(name);
    }

}
