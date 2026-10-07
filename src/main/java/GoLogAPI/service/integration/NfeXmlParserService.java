package GoLogAPI.service.integration;

import GoLogAPI.dto.integration.InvoiceImportItem;
import GoLogAPI.model.enums.TypeOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class NfeXmlParserService {

    private static final Logger log = LoggerFactory.getLogger(NfeXmlParserService.class);

    /**
     * Faz o parsing de um XML de NF-e da SEFAZ (procNFe ou NFe) e extrai os dados em InvoiceImportItem.
     */
    public InvoiceImportItem parseXml(String xmlContent) throws Exception {
        if (xmlContent == null || xmlContent.isBlank()) {
            throw new IllegalArgumentException("O conteúdo XML da NF-e está vazio.");
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xmlContent)));
        doc.getDocumentElement().normalize();

        return extractInvoiceData(doc);
    }

    /**
     * Faz o parsing de um XML a partir de um InputStream.
     */
    public InvoiceImportItem parseXml(InputStream inputStream) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);
        doc.getDocumentElement().normalize();

        return extractInvoiceData(doc);
    }

    private InvoiceImportItem extractInvoiceData(Document doc) {
        // 1. Chave de acesso (Id da tag infNFe)
        String accessKey = "";
        NodeList infNFeList = doc.getElementsByTagName("infNFe");
        if (infNFeList.getLength() > 0) {
            Element infNFe = (Element) infNFeList.item(0);
            String idAttr = infNFe.getAttribute("Id");
            if (idAttr != null && idAttr.startsWith("NFe")) {
                accessKey = idAttr.substring(3);
            } else if (idAttr != null) {
                accessKey = idAttr;
            }
        }
        if (accessKey.isBlank()) {
            accessKey = getTagValue(doc, "chNFe");
        }

        // 2. Identificação da NF-e (<ide>)
        String invoiceNumber = getTagValue(doc, "nNF");
        String series = getTagValue(doc, "serie");
        String dhEmiStr = getTagValue(doc, "dhEmi");
        LocalDateTime scheduledDate = parseNfeDate(dhEmiStr);

        // 3. Emitente (<emit>)
        String senderName = "";
        String senderDocument = "";
        NodeList emitList = doc.getElementsByTagName("emit");
        if (emitList.getLength() > 0) {
            Element emit = (Element) emitList.item(0);
            senderName = getTagValue(emit, "xNome");
            senderDocument = getTagValue(emit, "CNPJ");
            if (senderDocument.isBlank()) {
                senderDocument = getTagValue(emit, "CPF");
            }
        }

        // 4. Destinatário (<dest>)
        String customerName = "";
        String customerDocument = "";
        String customerEmail = getTagValue(doc, "email");
        String customerPhone = "";
        String street = "";
        String number = "S/N";
        String district = "";
        String city = "";
        String state = "";
        String cep = "";

        NodeList destList = doc.getElementsByTagName("dest");
        if (destList.getLength() > 0) {
            Element dest = (Element) destList.item(0);
            customerName = getTagValue(dest, "xNome");
            customerDocument = getTagValue(dest, "CNPJ");
            if (customerDocument.isBlank()) {
                customerDocument = getTagValue(dest, "CPF");
            }

            NodeList enderDestList = dest.getElementsByTagName("enderDest");
            if (enderDestList.getLength() > 0) {
                Element ender = (Element) enderDestList.item(0);
                street = getTagValue(ender, "xLgr");
                String nro = getTagValue(ender, "nro");
                if (!nro.isBlank()) number = nro;
                district = getTagValue(ender, "xBairro");
                city = getTagValue(ender, "xMun");
                state = getTagValue(ender, "UF");
                cep = getTagValue(ender, "CEP");
                customerPhone = getTagValue(ender, "fone");
            }
        }

        // 5. Valores e Carga (<total> e <transp>)
        Double invoiceValue = parseDouble(getTagValue(doc, "vNF"), 0.0);
        Double grossWeight = parseDouble(getTagValue(doc, "pesoB"), null);
        Double netWeight = parseDouble(getTagValue(doc, "pesoL"), null);
        Double weight = grossWeight != null ? grossWeight : (netWeight != null ? netWeight : 1.0);

        Double qVol = parseDouble(getTagValue(doc, "qVol"), 1.0);
        // Estimativa razoável de cubagem caso volume em m3 não venha explícito: 0.05m³ por volume
        Double volume = Math.max(0.1, Math.round(qVol * 0.05 * 100.0) / 100.0);

        // 6. Informações complementares (<infCpl>)
        String notes = getTagValue(doc, "infCpl");

        return new InvoiceImportItem(
                invoiceNumber,
                series,
                accessKey,
                TypeOperation.ENTREGA,
                weight,
                volume,
                invoiceValue,
                scheduledDate,
                notes,
                senderName,
                senderDocument,
                customerName,
                customerDocument,
                customerEmail,
                customerPhone,
                street,
                number,
                district,
                city,
                state,
                cep,
                null, // Latitude resolvida no importador
                null  // Longitude resolvida no importador
        );
    }

    private static String getTagValue(Element parent, String tagName) {
        if (parent == null) return "";
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() > 0 && nl.item(0) != null) {
            return nl.item(0).getTextContent().trim();
        }
        return "";
    }

    private static String getTagValue(Document doc, String tagName) {
        if (doc == null) return "";
        NodeList nl = doc.getElementsByTagName(tagName);
        if (nl.getLength() > 0 && nl.item(0) != null) {
            return nl.item(0).getTextContent().trim();
        }
        return "";
    }

    private static Double parseDouble(String value, Double defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static LocalDateTime parseNfeDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        }
        try {
            // Padrão SEFAZ: 2026-10-06T14:30:00-03:00
            OffsetDateTime odt = OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return odt.toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ex) {
                return LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
            }
        }
    }
}
