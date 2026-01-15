package com.altenburg.erp.controller;

import com.altenburg.erp.entity.Product;
import com.altenburg.erp.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> getAll() {
        return service.getAllProducts();
    }

    @PostMapping
    public Product add(@RequestBody Product product) {
        return service.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteProduct(id);
    }

    @GetMapping("/invoice")
    public void downloadInvoice(HttpServletResponse response) throws IOException {
        byte[] pdfBytes = service.generateInvoicePdf();
        
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Lieferschein.pdf");
        response.getOutputStream().write(pdfBytes);
    }
}
