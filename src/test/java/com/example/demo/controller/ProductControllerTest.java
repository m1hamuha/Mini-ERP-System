package com.example.demo.controller;

import com.example.demo.Product;
import com.example.demo.ProductService;
import com.example.demo.controller.ProductController;
import com.example.demo.dto.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService service;

    @InjectMocks
    private ProductController controller;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setName("Test Product 1");
        testProduct1.setQuantity(10);
        testProduct1.setPrice(19.99);

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Test Product 2");
        testProduct2.setQuantity(5);
        testProduct2.setPrice(29.99);
    }

    @Test
    void getAllProducts_shouldReturnPagedResponse() {
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 20), 2);
        when(service.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        ResponseEntity<PagedResponse<Product>> response = controller.getAllProducts(0, 20, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(0, response.getBody().getPage());
        assertEquals(2, response.getBody().getTotalElements());
        verify(service, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    void getById_shouldReturnProductWhenExists() {
        when(service.getProductById(1L)).thenReturn(Optional.of(testProduct1));

        ResponseEntity<Product> response = controller.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Product 1", response.getBody().getName());
        verify(service, times(1)).getProductById(1L);
    }

    @Test
    void getById_shouldReturnNotFoundWhenNotExists() {
        when(service.getProductById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = controller.getById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(service, times(1)).getProductById(99L);
    }

    @Test
    void search_shouldReturnPagedMatchingProducts() {
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 20), 2);
        when(service.searchByName(eq("Test"), any(Pageable.class))).thenReturn(productPage);

        ResponseEntity<PagedResponse<Product>> response = controller.search("Test", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(service, times(1)).searchByName(eq("Test"), any(Pageable.class));
    }

    @Test
    void create_shouldCreateAndReturnProduct() {
        when(service.saveProduct(testProduct1)).thenReturn(testProduct1);

        ResponseEntity<Product> response = controller.create(testProduct1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Test Product 1", response.getBody().getName());
        verify(service, times(1)).saveProduct(testProduct1);
    }

    @Test
    void update_shouldUpdateAndReturnProductWhenExists() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setQuantity(20);
        updatedProduct.setPrice(29.99);

        when(service.updateProduct(1L, updatedProduct)).thenReturn(Optional.of(testProduct1));

        ResponseEntity<Product> response = controller.update(1L, updatedProduct);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Product 1", response.getBody().getName());
        verify(service, times(1)).updateProduct(1L, updatedProduct);
    }

    @Test
    void update_shouldReturnNotFoundWhenNotExists() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");

        when(service.updateProduct(99L, updatedProduct)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = controller.update(99L, updatedProduct);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(service, times(1)).updateProduct(99L, updatedProduct);
    }

    @Test
    void delete_shouldReturnNoContentWhenExists() {
        when(service.deleteProduct(1L)).thenReturn(true);

        ResponseEntity<Void> response = controller.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service, times(1)).deleteProduct(1L);
    }

    @Test
    void delete_shouldReturnNotFoundWhenNotExists() {
        when(service.deleteProduct(99L)).thenReturn(false);

        ResponseEntity<Void> response = controller.delete(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(service, times(1)).deleteProduct(99L);
    }

    @Test
    void downloadInvoice_shouldReturnPdf() {
        byte[] pdfContent = "PDF content".getBytes();
        when(service.generateInvoicePdf()).thenReturn(pdfContent);

        ResponseEntity<byte[]> response = controller.downloadInvoice();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentType());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertArrayEquals(pdfContent, response.getBody());
        verify(service, times(1)).generateInvoicePdf();
    }

    @Test
    void getAllProducts_withDescendingSort_shouldReturnSortedResults() {
        List<Product> products = Arrays.asList(testProduct2, testProduct1);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 20), 2);
        when(service.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        ResponseEntity<PagedResponse<Product>> response = controller.getAllProducts(0, 20, "name", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service, times(1)).getAllProducts(any(Pageable.class));
    }
}
