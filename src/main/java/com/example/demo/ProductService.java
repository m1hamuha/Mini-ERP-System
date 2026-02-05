package com.example.demo;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return repository.findById(id);
    }

    public List<Product> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public Page<Product> searchByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Product saveProduct(Product product) {
        return repository.save(product);
    }

    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return repository.findById(id).map(existing -> {
            existing.setName(updatedProduct.getName());
            existing.setQuantity(updatedProduct.getQuantity());
            existing.setPrice(updatedProduct.getPrice());
            return repository.save(existing);
        });
    }

    public boolean deleteProduct(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public byte[] generateInvoicePdf() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("LIEFERSCHEIN", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 12);
            String invoiceNumber = "LS-" + System.currentTimeMillis();
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Dokument Nr.: " + invoiceNumber, fontSubtitle));
            document.add(new Paragraph("Datum: " + date, fontSubtitle));
            document.add(new Paragraph("Standort: Altenburg, Thüringen", fontSubtitle));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("_".repeat(80)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 2});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            addTableHeader(table, "Nr.", headerFont);
            addTableHeader(table, "Produktname", headerFont);
            addTableHeader(table, "Menge (Stk.)", headerFont);
            addTableHeader(table, "Preis (€)", headerFont);

            List<Product> products = repository.findAll();
            int counter = 1;
            double totalValue = 0;

            for (Product p : products) {
                table.addCell(String.valueOf(counter++));
                table.addCell(p.getName());
                table.addCell(String.valueOf(p.getQuantity()));
                table.addCell(String.format("%.2f", p.getPrice()));
                totalValue += p.getPrice() * p.getQuantity();
            }

            document.add(table);
            document.add(new Paragraph(" "));

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph total = new Paragraph(
                String.format("GESAMTWERT: %.2f €", totalValue), boldFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC);
            document.add(new Paragraph("Mit freundlichen Grüßen", footerFont));
            document.add(new Paragraph("Mini-ERP System | Altenburg", footerFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Fehler bei der PDF-Generierung: " + e.getMessage(), e);
        }
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        table.addCell(new Phrase(text, font));
    }
}
