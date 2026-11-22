package GoLogAPI.service;

import GoLogAPI.exception.BusinessException;
import GoLogAPI.model.Company;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyServiceGeofenceTest {

    @Test
    void vazioMantemZeroLimpaValorDefine() {
        Company c = new Company();
        CompanyService.applyGeofenceRadius(c, 300);
        assertEquals(300, c.getGeofenceRadiusMeters());
        CompanyService.applyGeofenceRadius(c, null);          // PATCH/cliente antigo sem o campo
        assertEquals(300, c.getGeofenceRadiusMeters());
        CompanyService.applyGeofenceRadius(c, 0);             // volta ao padrão da transportadora
        assertNull(c.getGeofenceRadiusMeters());
    }

    @Test
    void raioMenorQue100Recusado() {
        assertThrows(BusinessException.class, () -> CompanyService.applyGeofenceRadius(new Company(), 50));
        assertThrows(BusinessException.class, () -> CompanyService.applyGeofenceRadius(new Company(), 99));
        Company c = new Company();
        CompanyService.applyGeofenceRadius(c, 100);
        assertEquals(100, c.getGeofenceRadiusMeters());
    }
}
