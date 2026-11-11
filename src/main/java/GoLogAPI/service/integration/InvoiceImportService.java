package GoLogAPI.service.integration;

import java.util.Map;

import java.util.HashMap;

import GoLogAPI.dto.integration.InvoiceImportItem;
import GoLogAPI.dto.integration.InvoiceImportResponse;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.CompanyType;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.repository.*;
import GoLogAPI.service.webhook.WebhookDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceImportService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceImportService.class);

    private final ShipmentRepository shipmentRepository;
    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final ShipmentTypeRepository shipmentTypeRepository;
    private final TypeTransportRepository typeTransportRepository;
    private final UserRepository userRepository;
    private final WebhookDispatcherService webhookDispatcherService;
    private GoLogAPI.infra.tenant.TenantGuard tenantGuard;
    private GoLogAPI.service.webhook.ShipmentEventPublisher shipmentEventPublisher;

    @org.springframework.beans.factory.annotation.Autowired
    void setCollaborators(GoLogAPI.infra.tenant.TenantGuard tenantGuard,
                          GoLogAPI.service.webhook.ShipmentEventPublisher shipmentEventPublisher) {
        this.tenantGuard = tenantGuard;
        this.shipmentEventPublisher = shipmentEventPublisher;
    }

    public InvoiceImportService(ShipmentRepository shipmentRepository,
                                CompanyRepository companyRepository,
                                AddressRepository addressRepository,
                                ShipmentTypeRepository shipmentTypeRepository,
                                TypeTransportRepository typeTransportRepository,
                                UserRepository userRepository,
                                WebhookDispatcherService webhookDispatcherService) {
        this.shipmentRepository = shipmentRepository;
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.shipmentTypeRepository = shipmentTypeRepository;
        this.typeTransportRepository = typeTransportRepository;
        this.userRepository = userRepository;
        this.webhookDispatcherService = webhookDispatcherService;
    }

    @Transactional
    public InvoiceImportResponse importInvoices(List<InvoiceImportItem> items) {
        long startTime = System.currentTimeMillis();

        if (items == null || items.isEmpty()) {
            return new InvoiceImportResponse(0, 0, 0, List.of(), List.of(), 0L);
        }

        // Tenant que importa: ativo na requisição; na Visão Global do Master, a empresa do próprio usuário
        Company currentCompany = tenantGuard.ownerForWrite(null);
        UUID tenantId = currentCompany.getId();

        // Catálogos e responsável sempre do tenant (antes: o primeiro registro do banco inteiro)
        List<TypeTransport> transportTypes = typeTransportRepository.findAvailableForCompany(tenantId);
        List<ShipmentType> shipmentTypes = shipmentTypeRepository.findAvailableForCompany(tenantId);
        User responsible = TenantContext.getCurrentUserId() != null
                ? userRepository.findById(TenantContext.getCurrentUserId()).orElse(null) : null;
        if (responsible == null) {
            responsible = userRepository.findByCompanyId(tenantId).stream().findFirst().orElse(null);
        }
        if (transportTypes.isEmpty() || shipmentTypes.isEmpty() || responsible == null) {
            throw new GoLogAPI.exception.BusinessException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O tenant precisa ter ao menos um tipo de transporte, um tipo de carga e um usuário antes de importar remessas.");
        }

        List<Shipment> shipmentsToSave = new ArrayList<>();
        Map<String, Shipment> byInvoice = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int errorCount = 0;

        for (int i = 0; i < items.size(); i++) {
            InvoiceImportItem item = items.get(i);
            String invoiceId = item.invoiceNumber() != null && !item.invoiceNumber().isBlank() ? item.invoiceNumber() : ("Item #" + i);
            try {
                Company customer = resolveOrCreateCustomer(item, currentCompany);
                // Endereço da operação vem do próprio item (coleta no produtor, entrega no varejista...)
                Address operationAddress = resolveOperationAddress(item, customer, currentCompany);

                Double weight = (item.weight() != null && item.weight() > 0) ? item.weight() : 1.0;
                Double volume = (item.volume() != null && item.volume() > 0) ? item.volume() : 0.1;
                LocalDateTime scheduledDate = item.scheduledDate() != null
                        ? item.scheduledDate()
                        : LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
                TypeOperation typeOperation = item.typeOperation() != null ? item.typeOperation() : TypeOperation.ENTREGA;

                Shipment shipment = Shipment.builder()
                        .weight(weight)
                        .volume(volume)
                        .schedulind(scheduledDate)
                        .typeOperation(typeOperation)
                        .status(ShipmentStatus.PENDENTE)
                        .customer(customer)
                        .address(operationAddress)
                        .shipmentType(pickByName(shipmentTypes, item.shipmentTypeName(), ShipmentType::getName))
                        .typeTransport(pickByName(transportTypes, item.typeTransportName(), TypeTransport::getName))
                        .company(currentCompany)
                        .user(responsible)
                        .build();

                shipmentsToSave.add(shipment);
                if (item.invoiceNumber() != null) byInvoice.put(item.invoiceNumber().trim(), shipment);
            } catch (Exception e) {
                errorCount++;
                errors.add("Erro ao processar fatura " + invoiceId + ": " + e.getMessage());
                log.warn("Erro ao importar fatura {}: {}", invoiceId, e.getMessage());
            }
        }

        // Vincula ENTREGA -> COLETA de origem do mesmo lote (precedência coleta antes da entrega no otimizador)
        for (int i = 0; i < items.size(); i++) {
            InvoiceImportItem item = items.get(i);
            Shipment shipment = item.invoiceNumber() != null ? byInvoice.get(item.invoiceNumber().trim()) : null;
            if (shipment == null || shipment.getTypeOperation() != TypeOperation.ENTREGA) continue;
            String originKey = item.originInvoiceNumber();
            if ((originKey == null || originKey.isBlank()) && item.invoiceNumber().trim().toUpperCase().endsWith("-ENT")) {
                String inv = item.invoiceNumber().trim();
                originKey = inv.substring(0, inv.length() - 4) + "-COL";
            }
            Shipment origin = originKey != null ? byInvoice.get(originKey.trim()) : null;
            if (origin != null && origin.getTypeOperation() == TypeOperation.COLETA) {
                shipment.setOperationOrigem(origin);
            }
        }

        List<UUID> createdIds = new ArrayList<>();
        if (!shipmentsToSave.isEmpty()) {
            for (Shipment saved : shipmentRepository.saveAll(shipmentsToSave)) {
                createdIds.add(saved.getId());
                shipmentEventPublisher.publish("SHIPMENT_CREATED", saved);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        return new InvoiceImportResponse(items.size(), createdIds.size(), errorCount, createdIds, errors, duration);
    }

    private static <T> T pickByName(List<T> options, String name, java.util.function.Function<T, String> nameOf) {
        if (name != null && !name.isBlank()) {
            String wanted = name.trim();
            for (T option : options) {
                String n = nameOf.apply(option);
                if (n != null && n.trim().equalsIgnoreCase(wanted)) return option;
            }
        }
        return options.get(0);
    }

    /** Reaproveita um endereço do tenant com as mesmas coordenadas ou rua/número/cidade; senão cria. */
    private Address resolveOperationAddress(InvoiceImportItem item, Company customer, Company tenant) {
        boolean hasStreet = item.street() != null && !item.street().isBlank();
        boolean hasCoords = item.latitude() != null && !item.latitude().isBlank()
                && item.longitude() != null && !item.longitude().isBlank();
        if (!hasStreet && !hasCoords) {
            return customer.getAddress();
        }
        for (Address a : addressRepository.findByCompanyId(tenant.getId())) {
            boolean sameCoords = hasCoords && item.latitude().trim().equals(a.getLatitude()) && item.longitude().trim().equals(a.getLongitude());
            boolean samePlace = hasStreet && item.street().trim().equalsIgnoreCase(a.getStreet())
                    && String.valueOf(item.number()).trim().equalsIgnoreCase(a.getNumber())
                    && item.city() != null && item.city().trim().equalsIgnoreCase(a.getCity());
            if (sameCoords && (!hasStreet || samePlace) || (!hasCoords && samePlace)) return a;
        }
        Address address = new Address();
        address.setStreet(hasStreet ? item.street().trim() : customer.getAddress().getStreet());
        address.setNumber(item.number() != null && !item.number().isBlank() ? item.number().trim() : "S/N");
        address.setDistrict(item.district() != null && !item.district().isBlank() ? item.district().trim() : "Centro");
        address.setCity(item.city() != null && !item.city().isBlank() ? item.city().trim() : customer.getAddress().getCity());
        address.setState(item.state() != null && !item.state().isBlank() ? item.state().trim().toUpperCase() : customer.getAddress().getState());
        address.setCountry("Brasil");
        address.setCep(item.cep() != null && !item.cep().isBlank() ? item.cep().trim() : "00000000");
        address.setLatitude(hasCoords ? item.latitude().trim() : customer.getAddress().getLatitude());
        address.setLongitude(hasCoords ? item.longitude().trim() : customer.getAddress().getLongitude());
        address.setCompany(tenant);
        return addressRepository.save(address);
    }

    private Company resolveOrCreateCustomer(InvoiceImportItem item, Company currentCompany) {
        String rawDoc = item.customerDocument();
        String cleanDoc = rawDoc != null ? rawDoc.replaceAll("[^0-9]", "") : "";

        if (!cleanDoc.isBlank()) {
            // Cliente é procurado apenas na carteira do tenant que está importando
            UUID ownerId = currentCompany != null ? currentCompany.getId() : null;
            var existing = companyRepository.findFirstByCnpjCpfAndOwnerTenantId(cleanDoc, ownerId)
                    .or(() -> companyRepository.findFirstByCnpjCpfAndOwnerTenantId(rawDoc.trim(), ownerId));
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Se não existir, cadastra novo cliente com seu endereço
        String street = item.street() != null && !item.street().isBlank() ? item.street() : "Rua Não Informada";
        String number = item.number() != null && !item.number().isBlank() ? item.number() : "S/N";
        String district = item.district() != null && !item.district().isBlank() ? item.district() : "Centro";
        String city = item.city() != null && !item.city().isBlank() ? item.city() : "São Paulo";
        String state = item.state() != null && !item.state().isBlank() ? item.state() : "SP";
        String cep = item.cep() != null && !item.cep().isBlank() ? item.cep() : "00000000";

        // Coordenadas com fallback
        String lat = item.latitude() != null && !item.latitude().isBlank()
                ? item.latitude()
                : (currentCompany != null && currentCompany.getAddress() != null ? currentCompany.getAddress().getLatitude() : "-23.550520");
        String lon = item.longitude() != null && !item.longitude().isBlank()
                ? item.longitude()
                : (currentCompany != null && currentCompany.getAddress() != null ? currentCompany.getAddress().getLongitude() : "-46.633308");

        Address address = new Address();
        address.setStreet(street);
        address.setNumber(number);
        address.setDistrict(district);
        address.setCity(city);
        address.setState(state);
        address.setCountry("Brasil");
        address.setCep(cep);
        address.setLatitude(lat);
        address.setLongitude(lon);
        address.setCompany(currentCompany);

        Address savedAddress = addressRepository.save(address);

        String legalName = item.customerName() != null && !item.customerName().isBlank()
                ? item.customerName()
                : ("Cliente NF " + (item.invoiceNumber() != null ? item.invoiceNumber() : "Geral"));

        String docToSave = !cleanDoc.isBlank() ? cleanDoc : "00000000000";
        if (docToSave.length() < 11) {
            docToSave = String.format("%011d", Long.parseLong(docToSave.isEmpty() ? "0" : docToSave));
        }

        Company customer = new Company();
        customer.setLegalName(legalName);
        customer.setCnpjCpf(docToSave);
        customer.setIsCliente(true);
        customer.setPhoneNumber(item.customerPhone() != null && !item.customerPhone().isBlank() ? item.customerPhone() : "(11)999999999");
        customer.setEmail(item.customerEmail() != null && !item.customerEmail().isBlank() ? item.customerEmail() : "cliente@golog.com");
        customer.setAddress(savedAddress);
        customer.setCompanyType(CompanyType.CLIENT);
        customer.setIsMaster(false);
        customer.setOwnerTenant(currentCompany);

        return companyRepository.save(customer);
    }
}
