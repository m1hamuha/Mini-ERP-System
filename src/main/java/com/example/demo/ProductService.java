package com.example.demo;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Product entities.
 * Provides CRUD operations and business logic for products.
 */
@Service
public class ProductService {

    private final ProductRepository repository;

    /**
     * Constructor for ProductService.
     * 
     * @param repository the ProductRepository to use for database operations
     */
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves all products from the database.
     * 
     * @return List of all Product entities
     */
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    /**
     * Retrieves a product by its ID.
     * 
     * @param id the ID of the product to retrieve
     * @return Optional containing the Product if found, empty otherwise
     */
    public Optional<Product> getProductById(Long id) {
        return repository.findById(id);
    }

    /**
     * Searches for products by name (case-insensitive, partial match).
     * 
     * @param name the name or partial name to search for
     * @return List of matching Product entities
     */
    public List<Product> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Saves a new product to the database.
     * 
     * @param product the Product entity to save
     * @return the saved Product entity with generated ID
     */
    public Product saveProduct(Product product) {
        return repository.save(product);
    }

    /**
     * Updates an existing product.
     * 
     * @param id the ID of the product to update
     * @param updatedProduct the Product entity with updated values
     * @return Optional containing the updated Product if successful, empty if product not found
     */
    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return repository.findById(id).map(existing -> {
            existing.setName(updatedProduct.getName());
            existing.setQuantity(updatedProduct.getQuantity());
            existing.setPrice(updatedProduct.getPrice());
            return repository.save(existing);
        });
    }

    /**
     * Deletes a product by its ID.
     * 
     * @param id the ID of the product to delete
     * @return true if deletion was successful, false if product not found
     */
    public boolean deleteProduct(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Generates a PDF invoice containing all products.
     * The invoice includes product details, quantities, prices, and total value.
     * 
     * @return byte array containing the generated PDF document
     * @throws RuntimeException if PDF generation fails
     */
    public byte[] generateInvoicePdf() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Заголовок с датой и номером
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

            // Разделительная линия
            document.add(new Paragraph("_".repeat(80)));
            document.add(new Paragraph(" "));

            // Таблица товаров
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 2});
            
            // Заголовки таблицы
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            addTableHeader(table, "Nr.", headerFont);
            addTableHeader(table, "Produktname", headerFont);
            addTableHeader(table, "Menge (Stk.)", headerFont);
            addTableHeader(table, "Preis (€)", headerFont);

            // Данные
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
            
            // Общая стоимость
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph total = new Paragraph(
                String.format("GESAMTWERT: %.2f €", totalValue), boldFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            
            // Подпись
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

