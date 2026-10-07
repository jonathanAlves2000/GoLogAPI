package GoLogAPI.controller;

import GoLogAPI.dto.integration.InvoiceImportItem;
import GoLogAPI.dto.integration.InvoiceImportResponse;
import GoLogAPI.dto.integration.InvoiceJsonBatchRequest;
import GoLogAPI.service.integration.InvoiceImportService;
import GoLogAPI.service.integration.NfeXmlParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/integration/invoices", "/integration/invoices"})
@Tag(name = "Integração ERP/WMS & Importação de Cargas", description = "Leitura e importação de NF-e (XML) e faturas (JSON) para criação em lote de Remessas")
public class InvoiceIntegrationController {

    private final NfeXmlParserService nfeXmlParserService;
    private final InvoiceImportService invoiceImportService;

    public InvoiceIntegrationController(NfeXmlParserService nfeXmlParserService,
                                        InvoiceImportService invoiceImportService) {
        this.nfeXmlParserService = nfeXmlParserService;
        this.invoiceImportService = invoiceImportService;
    }

    @Operation(summary = "Importar NF-e via Upload de Arquivos XML",
               description = "Recebe um ou múltiplos arquivos XML de NF-e da SEFAZ, extrai clientes, endereços e volumes e cria Remessas em lote.")
    @PostMapping(value = "/xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceImportResponse> importXmlFiles(@RequestParam("files") MultipartFile[] files) {
        List<InvoiceImportItem> items = new ArrayList<>();
        List<String> parseErrors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                InvoiceImportItem item = nfeXmlParserService.parseXml(file.getInputStream());
                items.add(item);
            } catch (Exception e) {
                parseErrors.add("Erro no XML '" + file.getOriginalFilename() + "': " + e.getMessage());
            }
        }

        InvoiceImportResponse response = invoiceImportService.importInvoices(items);
        if (!parseErrors.isEmpty()) {
            response.errors().addAll(parseErrors);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Importar NF-e via Conteúdo XML em Texto",
               description = "Recebe o texto puro de um XML de NF-e da SEFAZ e cria a Remessa correspondente.")
    @PostMapping(value = "/xml-text", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<InvoiceImportResponse> importXmlText(@RequestBody String xmlContent) throws Exception {
        InvoiceImportItem item = nfeXmlParserService.parseXml(xmlContent);
        InvoiceImportResponse response = invoiceImportService.importInvoices(List.of(item));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Importar Faturas/Pedidos via JSON",
               description = "Endpoint padronizado para ERPs e WMS enviarem lotes de faturas e pedidos para criação automática de Remessas.")
    @PostMapping("/json")
    public ResponseEntity<InvoiceImportResponse> importJsonBatch(@RequestBody @Valid InvoiceJsonBatchRequest request) {
        InvoiceImportResponse response = invoiceImportService.importInvoices(request.invoices());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Visualizar Dados Extraídos de um XML (Preview sem Salvar)",
               description = "Faz o parsing de um arquivo XML de NF-e e retorna os campos extraídos sem persistir no banco.")
    @PostMapping(value = "/parse-xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceImportItem> parseXmlOnly(@RequestParam("file") MultipartFile file) throws Exception {
        InvoiceImportItem item = nfeXmlParserService.parseXml(file.getInputStream());
        return ResponseEntity.ok(item);
    }
}
