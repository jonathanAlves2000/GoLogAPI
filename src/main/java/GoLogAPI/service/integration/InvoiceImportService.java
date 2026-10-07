package GoLogAPI.service.integration;

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

        UUID currentTenant = TenantContext.getCurrentTenantId();
        Company currentCompany = currentTenant != null
                ? companyRepository.findById(currentTenant).orElse(null)
                : companyRepository.findFirstByIsMasterTrue().orElse(null);

        // Fallbacks de configuração
        ShipmentType defaultShipmentType = shipmentTypeRepository.findAll().stream().findFirst().orElse(null);
        TypeTransport defaultTypeTransport = typeTransportRepository.findAll().stream().findFirst().orElse(null);
        User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);

        List<Shipment> shipmentsToSave = new ArrayList<>();
        List<UUID> createdIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < items.size(); i++) {
            InvoiceImportItem item = items.get(i);
            try {
                // 1. Resolve ou cadastra o Cliente destinatário da carga
                Company customer = resolveOrCreateCustomer(item, currentCompany);

                // 2. Resolve endereço da entrega
                Address deliveryAddress = customer.getAddress();

                // 3. Monta a Remessa
                Double weight = (item.weight() != null && item.weight() > 0) ? item.weight() : 1.0;
                Double volume = (item.volume() != null && item.volume() > 0) ? item.volume() : 0.1;
                LocalDateTime scheduledDate = item.scheduledDate() != null
                        ? item.scheduledDate()
                        : LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);

                TypeOperation typeOperation = item.typeOperation() != null
                        ? item.typeOperation()
                        : TypeOperation.ENTREGA;

                Shipment shipment = Shipment.builder()
                        .weight(weight)
                        .volume(volume)
                        .schedulind(scheduledDate)
                        .typeOperation(typeOperation)
                        .status(ShipmentStatus.PENDENTE)
                        .customer(customer)
                        .address(deliveryAddress)
                        .shipmentType(defaultShipmentType)
                        .typeTransport(defaultTypeTransport)
                        .company(currentCompany)
                        .user(defaultUser)
                        .build();

                shipmentsToSave.add(shipment);
                successCount++;

            } catch (Exception e) {
                errorCount++;
                String invoiceId = item.invoiceNumber() != null ? item.invoiceNumber() : ("Item #" + i);
                errors.add("Erro ao processar fatura " + invoiceId + ": " + e.getMessage());
                log.warn("Erro ao importar fatura {}: {}", invoiceId, e.getMessage());
            }
        }

        // 4. Salvamento em lote no banco
        if (!shipmentsToSave.isEmpty()) {
            List<Shipment> savedList = shipmentRepository.saveAll(shipmentsToSave);
            for (Shipment saved : savedList) {
                createdIds.add(saved.getId());

                // 5. Dispara webhook SHIPMENT_CREATED para o ERP da empresa
                if (currentCompany != null) {
                    webhookDispatcherService.dispatch(currentCompany, "SHIPMENT_CREATED", saved);
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Lote de faturas/NF-e importado: total={}, sucesso={}, erros={}, tempo={}ms",
                items.size(), successCount, errorCount, duration);

        return new InvoiceImportResponse(
                items.size(),
                successCount,
                errorCount,
                createdIds,
                errors,
                duration
        );
    }

    private Company resolveOrCreateCustomer(InvoiceImportItem item, Company currentCompany) {
        String rawDoc = item.customerDocument();
        String cleanDoc = rawDoc != null ? rawDoc.replaceAll("[^0-9]", "") : "";

        if (!cleanDoc.isBlank()) {
            var existing = companyRepository.findByCnpjCpf(cleanDoc)
                    .or(() -> companyRepository.findByCnpjCpf(rawDoc.trim()));
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
        customer.setParentCompany(currentCompany);

        return companyRepository.save(customer);
    }
}
