package GoLogAPI.dto.shipment;

import GoLogAPI.dto.company.CompanyResponseShipment;
import GoLogAPI.model.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponseListPersonalized(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime shedulind,
        String status,

        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdAt", "createdBy", "updatedAt", "updatedBy", "active"})
        ShipmentType shipmentType,

        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdAt", "createdBy", "updatedAt", "updatedBy", "active"})
        TypeTransport typeTransport,

        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdAt", "createdBy", "updatedAt", "updatedBy", "active"})
        Address address,

        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdAt", "createdBy", "updatedAt", "updatedBy", "active", "address", "company"})
        Company customer,

        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "routeStop", "transport", "customer", "address"})
        Shipment operationOrigem,

        @JsonIgnoreProperties({
                "hibernateLazyInitializer", "handler",
                "createdAt", "createdBy", "updatedAt", "updatedBy", "active",
                "transporter", "shipments",
                "equipamentGroup.address",
                "equipamentGroup.createdAt",
                "equipamentGroup.createdBy",
                "equipamentGroup.updatedAt",
                "equipamentGroup.updatedBy"
        })
        Transport transport,

        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdAt", "createdBy", "updatedAt", "updatedBy", "active", "transport", "shipment"})
        RouteStop routeStop
) { }
