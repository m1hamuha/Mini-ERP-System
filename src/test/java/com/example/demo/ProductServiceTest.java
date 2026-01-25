package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

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
    void getAllProducts_shouldReturnAllProducts() {
        when(repository.findAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));

        List<Product> result = service.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Test Product 1", result.get(0).getName());
        assertEquals("Test Product 2", result.get(1).getName());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getProductById_shouldReturnProductWhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct1));

        Optional<Product> result = service.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Product 1", result.get().getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void getProductById_shouldReturnEmptyWhenNotExists() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = service.getProductById(99L);

        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(99L);
    }

    @Test
    void searchByName_shouldReturnMatchingProducts() {
        when(repository.findByNameContainingIgnoreCase("Test")).thenReturn(Arrays.asList(testProduct1, testProduct2));

        List<Product> result = service.searchByName("Test");

        assertEquals(2, result.size());
        verify(repository, times(1)).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void saveProduct_shouldSaveAndReturnProduct() {
        when(repository.save(testProduct1)).thenReturn(testProduct1);

        Product result = service.saveProduct(testProduct1);

        assertEquals("Test Product 1", result.getName());
        verify(repository, times(1)).save(testProduct1);
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProductWhenExists() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setQuantity(20);
        updatedProduct.setPrice(29.99);

        when(repository.findById(1L)).thenReturn(Optional.of(testProduct1));
        when(repository.save(testProduct1)).thenReturn(testProduct1);

        Optional<Product> result = service.updateProduct(1L, updatedProduct);

        assertTrue(result.isPresent());
        assertEquals("Updated Product", result.get().getName());
        assertEquals(20, result.get().getQuantity());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(testProduct1);
    }

    @Test
    void updateProduct_shouldReturnEmptyWhenNotExists() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = service.updateProduct(99L, updatedProduct);

        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteProduct_shouldReturnTrueWhenExists() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        boolean result = service.deleteProduct(1L);

        assertTrue(result);
        verify(repository, times(1)).existsById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_shouldReturnFalseWhenNotExists() {
        when(repository.existsById(99L)).thenReturn(false);

        boolean result = service.deleteProduct(99L);

        assertFalse(result);
        verify(repository, times(1)).existsById(99L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void generateInvoicePdf_shouldGeneratePdfSuccessfully() {
        when(repository.findAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));

        byte[] result = service.generateInvoicePdf();

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(repository, times(1)).findAll();
    }

    @Test
    void generateInvoicePdf_shouldThrowExceptionWhenErrorOccurs() {
        when(repository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> service.generateInvoicePdf());
        verify(repository, times(1)).findAll();
    }
}