package GoLogAPI.service.integration;

import GoLogAPI.dto.integration.InvoiceImportItem;
import GoLogAPI.model.enums.TypeOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NfeXmlParserServiceTest {

    private final NfeXmlParserService parser = new NfeXmlParserService();

    @Test
    @DisplayName("Deve extrair corretamente dados de uma NF-e modelo 55 da SEFAZ")
    void shouldParseNfeXmlSuccessfully() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <nfeProc versao="4.00" xmlns="http://www.portalfiscal.inf.br/nfe">
                    <NFe>
                        <infNFe Id="NFe35230912345678000190550010000123451000123456" versao="4.00">
                            <ide>
                                <nNF>12345</nNF>
                                <serie>1</serie>
                                <dhEmi>2026-10-06T14:30:00-03:00</dhEmi>
                            </ide>
                            <emit>
                                <CNPJ>12345678000190</CNPJ>
                                <xNome>FABRICA DE ALIMENTOS BRASIL LTDA</xNome>
                            </emit>
                            <dest>
                                <CNPJ>98765432000101</CNPJ>
                                <xNome>SUPERMERCADO CENTRAL DO VALE</xNome>
                                <enderDest>
                                    <xLgr>Av. Brasil</xLgr>
                                    <nro>1500</nro>
                                    <xBairro>Jardim América</xBairro>
                                    <xMun>Campinas</xMun>
                                    <UF>SP</UF>
                                    <CEP>13080000</CEP>
                                    <fone>1933334444</fone>
                                </enderDest>
                                <email>compras@supercentral.com.br</email>
                            </dest>
                            <total>
                                <ICMSTot>
                                    <vNF>15750.50</vNF>
                                </ICMSTot>
                            </total>
                            <transp>
                                <vol>
                                    <qVol>20</qVol>
                                    <pesoL>450.000</pesoL>
                                    <pesoB>480.000</pesoB>
                                </vol>
                            </transp>
                            <infAdic>
                                <infCpl>Entrega urgente no setor de recebimento doca 3</infCpl>
                            </infAdic>
                        </infNFe>
                    </NFe>
                </nfeProc>
                """;

        InvoiceImportItem item = parser.parseXml(xml);

        assertNotNull(item);
        assertEquals("12345", item.invoiceNumber());
        assertEquals("1", item.series());
        assertEquals("35230912345678000190550010000123451000123456", item.accessKey());
        assertEquals(TypeOperation.ENTREGA, item.typeOperation());
        assertEquals(480.0, item.weight());
        assertTrue(item.volume() > 0);
        assertEquals(15750.50, item.value());
        assertEquals("FABRICA DE ALIMENTOS BRASIL LTDA", item.senderName());
        assertEquals("12345678000190", item.senderDocument());
        assertEquals("SUPERMERCADO CENTRAL DO VALE", item.customerName());
        assertEquals("98765432000101", item.customerDocument());
        assertEquals("compras@supercentral.com.br", item.customerEmail());
        assertEquals("Av. Brasil", item.street());
        assertEquals("1500", item.number());
        assertEquals("Jardim América", item.district());
        assertEquals("Campinas", item.city());
        assertEquals("SP", item.state());
        assertEquals("13080000", item.cep());
        assertTrue(item.notes().contains("doca 3"));
    }
}
