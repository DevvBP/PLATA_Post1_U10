package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("Debe guardar y retornar el producto cuando los datos son validos")
    void crear_conDatosValidos_retornaProductoGuardado() {
        Producto productoInput = new Producto("Teclado Mecanico", 79.99, "Perifericos");
        Producto productoGuardado = new Producto(1L, "Teclado Mecanico", 79.99, "Perifericos");

        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        Producto resultado = productoService.crear(productoInput);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Teclado Mecanico", resultado.getNombre());
        assertEquals(79.99, resultado.getPrecio());
        assertEquals("Perifericos", resultado.getCategoria());
        verify(productoRepository, times(1)).save(productoInput);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el producto es nulo")
    void crear_conProductoNulo_lanzaIllegalArgumentException() {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            productoService.crear(null);
        });

        assertEquals("El producto no puede ser nulo", excepcion.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el nombre es nulo o vacio")
    void crear_conNombreInvalido_lanzaIllegalArgumentException() {
        Producto productoNombreNulo = new Producto(null, 50.0, "Hogar");
        Producto productoNombreVacio = new Producto("   ", 50.0, "Hogar");

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(productoNombreNulo));
        assertThrows(IllegalArgumentException.class, () -> productoService.crear(productoNombreVacio));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el precio es menor o igual a cero")
    void crear_conPrecioInvalido_lanzaIllegalArgumentException() {
        Producto productoPrecioCero = new Producto("Monitor", 0.0, "Electronica");
        Producto productoPrecioNegativo = new Producto("Monitor", -15.0, "Electronica");

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(productoPrecioCero));
        assertThrows(IllegalArgumentException.class, () -> productoService.crear(productoPrecioNegativo));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar el producto cuando el ID existe")
    void buscarPorId_existente_retornaProducto() {
        Producto productoExistente = new Producto(1L, "Mouse Inalambrico", 25.50, "Perifericos");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));

        Producto resultado = productoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Mouse Inalambrico", resultado.getNombre());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException cuando el ID no existe")
    void buscarPorId_noExistente_lanzaEntityNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException excepcion = assertThrows(EntityNotFoundException.class, () -> {
            productoService.buscarPorId(99L);
        });

        assertTrue(excepcion.getMessage().contains("99"));
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el ID es nulo")
    void buscarPorId_conIdNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> productoService.buscarPorId(null));
        verify(productoRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Debe listar productos por categoria cuando se especifica")
    void listarPorCategoria_conCategoriaEspecifica_retornaLista() {
        List<Producto> lista = Arrays.asList(
                new Producto(1L, "Laptop", 1200.0, "Computadores"),
                new Producto(2L, "Tablet", 450.0, "Computadores")
        );
        when(productoRepository.findByCategoria("Computadores")).thenReturn(lista);

        List<Producto> resultado = productoService.listarPorCategoria("Computadores");

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(productoRepository, times(1)).findByCategoria("Computadores");
    }

    @Test
    @DisplayName("Debe listar todos los productos cuando la categoria es nula o vacia")
    void listarPorCategoria_conCategoriaNulaOVacia_retornaTodos() {
        List<Producto> lista = Collections.singletonList(
                new Producto(1L, "Silla", 150.0, "Muebles")
        );
        when(productoRepository.findAll()).thenReturn(lista);

        List<Producto> resultadoNula = productoService.listarPorCategoria(null);
        List<Producto> resultadoVacia = productoService.listarPorCategoria("  ");

        assertEquals(1, resultadoNula.size());
        assertEquals(1, resultadoVacia.size());
        verify(productoRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("Debe listar todos los productos mediante listarTodos")
    void listarTodos_retornaListaCompleta() {
        List<Producto> lista = Collections.singletonList(
                new Producto(1L, "Audifonos", 60.0, "Audio")
        );
        when(productoRepository.findAll()).thenReturn(lista);

        List<Producto> resultado = productoService.listarTodos();

        assertEquals(1, resultado.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe actualizar el precio correctamente cuando los datos son validos")
    void actualizarPrecio_conPrecioValido_actualizaYGuarda() {
        Producto productoOriginal = new Producto(1L, "Camara Web", 80.0, "Video");
        Producto productoActualizado = new Producto(1L, "Camara Web", 95.0, "Video");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoOriginal));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        Producto resultado = productoService.actualizarPrecio(1L, 95.0);

        assertNotNull(resultado);
        assertEquals(95.0, resultado.getPrecio());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(productoOriginal);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al actualizar con precio menor o igual a cero")
    void actualizarPrecio_conPrecioInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> productoService.actualizarPrecio(1L, 0.0));
        assertThrows(IllegalArgumentException.class, () -> productoService.actualizarPrecio(1L, -10.0));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException al actualizar el precio de un ID inexistente")
    void actualizarPrecio_conIdInexistente_lanzaEntityNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productoService.actualizarPrecio(99L, 100.0));
        verify(productoRepository, times(1)).findById(99L);
        verify(productoRepository, never()).save(any());
    }
}
