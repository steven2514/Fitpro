package com.proyecto.fitpro.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");
        cliente.setAltura(1.75);
        cliente.setPeso(80.0);
    }

    @Test
    void testCalcularIMC() {
        Double imc = cliente.calcularIMC();
        assertNotNull(imc);
        assertEquals(26.12, Math.round(imc * 100.0) / 100.0);
    }

    @Test
    void testObtenerCategoriaIMC() {
        cliente.setPeso(65.0);
        String categoria = cliente.obtenerCategoriaIMC();
        assertEquals("Peso normal", categoria);
    }

    @Test
    void testObtenerCategoriaSobrepeso() {
        cliente.setPeso(90.0);
        String categoria = cliente.obtenerCategoriaIMC();
        assertEquals("Sobrepeso", categoria);
    }

    @Test
    void testGetPesoIdeal() {
        Double pesoIdeal = cliente.getPesoIdeal();
        assertNotNull(pesoIdeal);
        assertEquals(68.91, Math.round(pesoIdeal * 100.0) / 100.0);
    }

    @Test
    void testCalcularIMCConAlturaCero() {
        cliente.setAltura(0.0);
        Double imc = cliente.calcularIMC();
        assertNull(imc);
    }

    @Test
    void testGetIMCFormateado() {
        String imcFormato = cliente.getIMCFormateado();
        assertNotNull(imcFormato);
        assertTrue(imcFormato.contains("."));
    }
}
