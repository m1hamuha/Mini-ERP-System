package com.altenburg.erp.service;

import com.altenburg.erp.entity.Product;
import com.altenburg.erp.repository.ProductRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Product saveProduct(Product product) {
        return repository.save(product);
    }

    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    public byte[] generateInvoicePdf() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Заголовок
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Lieferschein / Warenbestand", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Таблица
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Produktname");
            table.addCell("Menge (Stk.)");
            table.addCell("Preis (€)");

            List<Product> products = repository.findAll();
            for (Product p : products) {
                table.addCell(p.getName());
                table.addCell(String.valueOf(p.getQuantity()));
                table.addCell(String.format("%.2f €", p.getPrice()));
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Vielen Dank für Ihre Arbeit. System generiert in Altenburg.", 
                    FontFactory.getFont(FontFactory.HELVETICA, 10)));
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации PDF", e);
        }
    }
}
