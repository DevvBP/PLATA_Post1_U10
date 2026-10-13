package com.universidad.catalogo.repository;

import com.universidad.catalogo.model.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    @DisplayName("Debe encontrar productos filtrando por categoria existente")
    void findByCategoria_categoriaExistente_retornaProductosCoincidentes() {
        Producto p1 = new Producto("Teclado Mecanico", 85.0, "Perifericos");
        Producto p2 = new Producto("Raton Optico", 30.0, "Perifericos");
        Producto p3 = new Producto("Monitor 24 Pulgadas", 190.0, "Monitores");

        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush();

        List<Producto> resultado = productoRepository.findByCategoria("Perifericos");

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> "Perifericos".equals(p.getCategoria())));
    }

    @Test
    @DisplayName("Debe retornar lista vacia cuando no existen productos en la categoria")
    void findByCategoria_categoriaInexistente_retornaListaVacia() {
        Producto p1 = new Producto("Impresora Laser", 210.0, "Oficina");
        entityManager.persistAndFlush(p1);

        List<Producto> resultado = productoRepository.findByCategoria("Laptops");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe guardar y recuperar un producto por su ID generado")
    void guardarYBuscarPorId_retornaProductoCorrecto() {
        Producto nuevo = new Producto("Disco SSD 1TB", 75.0, "Almacenamiento");
        Producto guardado = productoRepository.save(nuevo);

        assertNotNull(guardado.getId());

        Optional<Producto> recuperado = productoRepository.findById(guardado.getId());
        assertTrue(recuperado.isPresent());
        assertEquals("Disco SSD 1TB", recuperado.get().getNombre());
        assertEquals(75.0, recuperado.get().getPrecio());
    }

    @Test
    @DisplayName("Debe eliminar un producto persistido")
    void eliminar_remueveProductoDeLaBaseDeDatos() {
        Producto producto = new Producto("Memoria RAM 16GB", 45.0, "Componentes");
        Producto persistido = entityManager.persistAndFlush(producto);

        productoRepository.deleteById(persistido.getId());
        entityManager.flush();

        Optional<Producto> eliminado = productoRepository.findById(persistido.getId());
        assertFalse(eliminado.isPresent());
    }
}
