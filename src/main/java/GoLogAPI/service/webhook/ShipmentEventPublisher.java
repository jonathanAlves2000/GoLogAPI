package GoLogAPI.service.webhook;

import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publica eventos do ciclo de vida das remessas no webhook do tenant dono (Company.webhookUrl).
 *
 * <p>Eventos: {@code SHIPMENT_CREATED}, {@code SHIPMENT_PLANNED} (entrou numa rota), {@code SHIPMENT_UNPLANNED}
 * (transporte cancelado, volta a PENDENTE), {@code SHIPMENT_STARTED}, {@code SHIPMENT_DELIVERED} e
 * {@code SHIPMENT_OCCURRENCE}. O corpo é um envelope {@code {event, timestamp, companyId, data}} assinado
 * com HMAC-SHA256 no header {@code X-GoLog-Signature} (ver {@link WebhookDispatcherService}).</p>
 *
 * <p>O {@code data} é um resumo estável (não a entidade JPA), para que integrações não quebrem quando o
 * modelo interno mudar.</p>
 */
@Component
public class ShipmentEventPublisher {

    private final WebhookDispatcherService dispatcher;

    public ShipmentEventPublisher(WebhookDispatcherService dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void publish(String event, Shipment shipment, RouteStop stop, Map<String, Object> extra) {
        if (shipment == null || shipment.getCompany() == null) return;
        dispatcher.dispatch(shipment.getCompany(), event, summary(shipment, stop, extra));
    }

    public void publish(String event, Shipment shipment) {
        publish(event, shipment, null, null);
    }

    public static Map<String, Object> summary(Shipment s, RouteStop stop, Map<String, Object> extra) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("shipmentId", s.getId());
        data.put("typeOperation", s.getTypeOperation());
        data.put("status", s.getStatus());
        data.put("scheduledDate", s.getSchedulind());
        data.put("weight", s.getWeight());
        data.put("volume", s.getVolume());
        data.put("customerId", s.getCustomer() != null ? s.getCustomer().getId() : null);
        data.put("customerName", s.getCustomer() != null ? s.getCustomer().getLegalName() : null);
        data.put("originShipmentId", s.getOperationOrigem() != null ? s.getOperationOrigem().getId() : null);
        if (stop != null) {
            Transport t = stop.getTransport();
            data.put("sequenceOrder", stop.getSequenceOrder());
            if (t != null) {
                data.put("transportId", t.getId());
                data.put("codeTransport", t.getCodeTransport());
                data.put("driverName", t.getDriver() != null && t.getDriver().getUser() != null ? t.getDriver().getUser().getName() : null);
                data.put("vehiclePlate", t.getEquipamentGroup() != null && t.getEquipamentGroup().getEquipament1() != null
                        ? t.getEquipamentGroup().getEquipament1().getPlate() : null);
            }
        }
        if (extra != null) data.putAll(extra);
        return data;
    }
}
