package com.altenburg.erp.controller;

import com.altenburg.erp.entity.Product;
import com.altenburg.erp.repository.ProductRepository; // Создай интерфейс Repository сам (extends JpaRepository)
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Product add(@RequestBody Product product) {
        return repository.save(product);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // ТВОЙ КОЗЫРЬ: Генерация PDF накладной
    @GetMapping("/invoice")
    public void downloadInvoice(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Lieferschein.pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        
        // Заголовок на немецком
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Lieferschein / Warenbestand", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Отступ

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
        
        // Футер
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Vielen Dank für Ihre Arbeit. System generiert in Altenburg.", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        
        document.close();
    }
}
