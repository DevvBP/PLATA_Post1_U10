package com.universidad.catalogo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/productos debe retornar 201 Created al crear un producto valido")
    void crear_conProductoValido_retorna201Created() throws Exception {
        Producto nuevo = new Producto("Smartphone Pro", 899.99, "Telefonia");
        Producto guardado = new Producto(1L, "Smartphone Pro", 899.99, "Telefonia");

        when(productoService.crear(any(Producto.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Smartphone Pro"))
                .andExpect(jsonPath("$.precio").value(899.99))
                .andExpect(jsonPath("$.categoria").value("Telefonia"));
    }

    @Test
    @DisplayName("POST /api/productos debe retornar 400 Bad Request cuando el servicio lanza IllegalArgumentException")
    void crear_conDatosInvalidos_retorna400BadRequest() throws Exception {
        Producto invalido = new Producto("", -50.0, "Telefonia");

        when(productoService.crear(any(Producto.class)))
                .thenThrow(new IllegalArgumentException("El precio del producto debe ser mayor a 0"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("El precio del producto debe ser mayor a 0"));
    }

    @Test
    @DisplayName("GET /api/productos/{id} debe retornar 200 OK cuando el producto existe")
    void buscarPorId_conIdExistente_retorna200Ok() throws Exception {
        Producto producto = new Producto(10L, "Monitor 4K", 450.0, "Monitores");

        when(productoService.buscarPorId(10L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nombre").value("Monitor 4K"))
                .andExpect(jsonPath("$.precio").value(450.0))
                .andExpect(jsonPath("$.categoria").value("Monitores"));
    }

    @Test
    @DisplayName("GET /api/productos/{id} debe retornar 404 Not Found cuando el servicio lanza EntityNotFoundException")
    void buscarPorId_conIdInexistente_retorna404NotFound() throws Exception {
        when(productoService.buscarPorId(999L))
                .thenThrow(new EntityNotFoundException("Producto no encontrado con id: 999"));

        mockMvc.perform(get("/api/productos/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con id: 999"));
    }

    @Test
    @DisplayName("GET /api/productos?categoria=Computadores debe retornar 200 OK con los productos filtrados")
    void listar_conFiltroCategoria_retorna200Ok() throws Exception {
        List<Producto> lista = Arrays.asList(
                new Producto(1L, "Laptop Gamer", 1500.0, "Computadores"),
                new Producto(2L, "PC de Escritorio", 1100.0, "Computadores")
        );

        when(productoService.listarPorCategoria(eq("Computadores"))).thenReturn(lista);

        mockMvc.perform(get("/api/productos")
                        .param("categoria", "Computadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Laptop Gamer"))
                .andExpect(jsonPath("$[1].nombre").value("PC de Escritorio"));
    }

    @Test
    @DisplayName("GET /api/productos sin parametros debe retornar 200 OK con todos los productos")
    void listar_sinFiltro_retorna200Ok() throws Exception {
        List<Producto> lista = Arrays.asList(
                new Producto(1L, "Mouse", 20.0, "Perifericos")
        );

        when(productoService.listarPorCategoria(null)).thenReturn(lista);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Mouse"));
    }
}
