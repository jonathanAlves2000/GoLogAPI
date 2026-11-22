package GoLogAPI.service;

import GoLogAPI.exception.BusinessException;
import GoLogAPI.model.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressServiceGeofenceTest {

    @Test
    void vazioMantemZeroLimpaValorDefine() {
        Address a = new Address();
        AddressService.applyGeofenceRadius(a, 300);
        assertEquals(300, a.getGeofenceRadiusMeters());
        AddressService.applyGeofenceRadius(a, null);          // tela de Empresas não envia o campo
        assertEquals(300, a.getGeofenceRadiusMeters());
        AddressService.applyGeofenceRadius(a, 0);             // volta ao padrão da transportadora
        assertNull(a.getGeofenceRadiusMeters());
    }

    @Test
    void raioMenorQue20Recusado() {
        assertThrows(BusinessException.class, () -> AddressService.applyGeofenceRadius(new Address(), 10));
    }
}
