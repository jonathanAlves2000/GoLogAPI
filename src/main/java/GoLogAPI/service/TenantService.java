package GoLogAPI.service;

import GoLogAPI.dto.tenant.TenantCreateRequest;
import GoLogAPI.dto.tenant.TenantResponse;
import GoLogAPI.dto.tenant.TenantSummaryResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.model.Address;
import GoLogAPI.model.Company;
import GoLogAPI.model.User;
import GoLogAPI.model.UserProfile;
import GoLogAPI.model.enums.CompanyType;
import GoLogAPI.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TenantService {

    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final EquipamentRepository equipamentRepository;
    private final ShipmentRepository shipmentRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(
            CompanyRepository companyRepository,
            AddressRepository addressRepository,
            UserRepository userRepository,
            EquipamentRepository equipamentRepository,
            ShipmentRepository shipmentRepository,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.equipamentRepository = equipamentRepository;
        this.shipmentRepository = shipmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TenantResponse createTenant(TenantCreateRequest request) {
        if (!TenantContext.isMaster()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas a administração GoLog Master pode criar novos tenants.");
        }

        if (companyRepository.existsByCnpjCpf(request.cnpjCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ/CPF de empresa já cadastrado: " + request.cnpjCpf());
        }

        if (userRepository.existsByEmail(request.adminEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail de administrador já em uso: " + request.adminEmail());
        }

        if (userRepository.existsByCpf(request.adminCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF de administrador já em uso: " + request.adminCpf());
        }

        Company parentCompany = null;
        if (request.companyType() == CompanyType.TENANT_BRANCH) {
            if (request.parentCompanyId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant do tipo filial (TENANT_BRANCH) exige uma empresa matriz (parentCompanyId).");
            }
            parentCompany = companyRepository.findById(request.parentCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa matriz não encontrada: ", request.parentCompanyId()));
        }

        // 1. Criar Endereço
        Address address = new Address();
        address.setCep(request.cep());
        address.setStreet(request.street());
        address.setNumber(request.number());
        address.setDistrict(request.district() != null ? request.district() : "Centro");
        address.setCity(request.city() != null ? request.city() : "São Paulo");
        address.setState(request.state() != null ? request.state() : "SP");
        address.setCountry("Brasil");
        address.setComplement(request.complement());
        address.setLatitude(request.latitude() != null && !request.latitude().isBlank() ? request.latitude() : "-23.550520");
        address.setLongitude(request.longitude() != null && !request.longitude().isBlank() ? request.longitude() : "-46.633308");
        Address savedAddress = addressRepository.save(address);

        // 2. Criar Empresa (Tenant)
        Company company = new Company();
        company.setLegalName(request.legalName());
        company.setCnpjCpf(request.cnpjCpf());
        company.setPhoneNumber(request.phoneNumber());
        company.setEmail(request.email());
        company.setIsCliente(false);
        company.setIsMaster(false);
        company.setCompanyType(request.companyType());
        company.setParentCompany(parentCompany);
        company.setAddress(savedAddress);
        Company savedCompany = companyRepository.save(company);

        // 3. Criar Usuário Administrador
        User adminUser = new User();
        adminUser.setName(request.adminName());
        adminUser.setEmail(request.adminEmail());
        adminUser.setPassword(passwordEncoder.encode(request.adminPassword()));
        adminUser.setCpf(request.adminCpf());
        adminUser.setUserProfile(UserProfile.ADMIN);
        adminUser.setCompany(savedCompany);
        userRepository.save(adminUser);

        return mapToTenantResponse(savedCompany);
    }

    public List<TenantResponse> getAllTenants() {
        if (TenantContext.isMaster()) {
            List<Company> tenants = companyRepository.findByCompanyTypeIn(
                    List.of(CompanyType.TENANT_MASTER, CompanyType.TENANT_HEADQUARTER, CompanyType.TENANT_BRANCH)
            );
            return tenants.stream().map(this::mapToTenantResponse).toList();
        } else {
            UUID userCompId = TenantContext.getUserCompanyId();
            if (userCompId == null) {
                return List.of();
            }
            Company current = companyRepository.findById(userCompId).orElse(null);
            if (current == null) {
                return List.of();
            }
            List<Company> list = new java.util.ArrayList<>();
            list.add(current);
            list.addAll(companyRepository.findByParentCompanyId(current.getId()));
            return list.stream().map(this::mapToTenantResponse).toList();
        }
    }

    public TenantResponse getTenantById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: ", id));
        return mapToTenantResponse(company);
    }

    public List<TenantResponse> getBranches(UUID parentCompanyId) {
        return companyRepository.findByParentCompanyId(parentCompanyId)
                .stream()
                .map(this::mapToTenantResponse)
                .toList();
    }

    public TenantSummaryResponse getSummary() {
        long totalTenants = companyRepository.findByCompanyType(CompanyType.TENANT_HEADQUARTER).size();
        long totalBranches = companyRepository.findByCompanyType(CompanyType.TENANT_BRANCH).size();
        long totalUsers = userRepository.count();
        long totalVehicles = equipamentRepository.count();
        long totalShipments = shipmentRepository.count();

        return new TenantSummaryResponse(totalTenants, totalBranches, totalUsers, totalVehicles, totalShipments);
    }

    private TenantResponse mapToTenantResponse(Company company) {
        String parentName = company.getParentCompany() != null ? company.getParentCompany().getLegalName() : null;
        UUID parentId = company.getParentCompany() != null ? company.getParentCompany().getId() : null;
        String city = company.getAddress() != null ? company.getAddress().getCity() : null;
        String state = company.getAddress() != null ? company.getAddress().getState() : null;
        int branchCount = company.getBranches() != null ? company.getBranches().size() : 0;

        return new TenantResponse(
                company.getId(),
                company.getLegalName(),
                company.getCnpjCpf(),
                company.getPhoneNumber(),
                company.getEmail(),
                company.getCompanyType(),
                company.getIsMaster(),
                parentId,
                parentName,
                city,
                state,
                branchCount,
                company.getCreatedAt(),
                company.getActive()
        );
    }
}
