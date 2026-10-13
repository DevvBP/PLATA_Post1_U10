# Catálogo de Productos - Laboratorio Unidad 10: Programación Web

Proyecto backend desarrollado con **Spring Boot 3.2.x**, **Java 17** y **Maven**, enfocado en la implementación de una arquitectura limpia en capas, aseguramiento de la calidad mediante pruebas automatizadas (unitarias, integración y API) y análisis de cobertura de código con **JaCoCo**.

---

## 1. Arquitectura y Tecnologías del Proyecto

- **Lenguaje:** Java 17 (LTS)
- **Framework:** Spring Boot 3.2.5
  - *Spring Web:* Exposición de API RESTful.
  - *Spring Data JPA:* Abstracción de acceso a datos y repositorios.
  - *Spring Validation:* Validación declarativa de entidades.
- **Persistencia:** Base de datos relacional H2 en memoria.
- **Testing & QA:**
  - *JUnit 5 (Jupiter)* y *Mockito* (Pruebas unitarias de servicios).
  - *@DataJpaTest* con *TestEntityManager* (Pruebas de persistencia JPA).
  - *@WebMvcTest* con *MockMvc* (Pruebas de capa controladora HTTP).
  - *JaCoCo Maven Plugin 0.8.11* (Verificación de cobertura mínima del 70%).
  - *Postman & Newman* (Pruebas de contrato y end-to-end de API).

---

## 2. Decisiones de Diseño y Justificaciones Técnicas

### 2.1. Base de Datos H2 en Memoria por Diseño
Se seleccionó **H2 Database** en modo en memoria (`jdbc:h2:mem:catalogodb`) tanto para el entorno de desarrollo local como para las pruebas automatizadas por las siguientes razones de ingeniería de software:
- **Idempotencia y aislamiento:** Cada inicio del servidor o batería de pruebas genera un entorno de base de datos limpio, garantizando que el estado previo no contamine los resultados.
- **Cero dependencias de infraestructura externa:** El proyecto puede ser clonado, compilado y ejecutado en cualquier máquina o pipeline de CI/CD sin necesidad de configurar servidores de bases de datos externos (PostgreSQL, MySQL ni contenedores Docker pesados).
- **Alta velocidad de ejecución:** Al operar 100% en memoria RAM, los ciclos de compilación, inserción y verificación en pruebas se ejecutan en milisegundos.

### 2.2. Diseño por Contrato en `buscarPorId` y Manejo de Excepciones
El método `buscarPorId(Long id)` en `ProductoService` implementa el principio de **Fallo Temprano (Fail-Fast)**:
- En lugar de devolver un valor `null` o delegar un `Optional` hacia el cliente, el servicio lanza de forma explícita una excepción `jakarta.persistence.EntityNotFoundException` cuando el recurso solicitado no existe.
- Esto formaliza un contrato claro: la invocación del método garantiza el retorno de una entidad válida o una ruptura controlada mediante excepción tipada.
- En la capa web, `ProductoController` intercepta dicha excepción mediante `@ExceptionHandler(EntityNotFoundException.class)` y la transforma limpiamente en una respuesta HTTP estándar **404 Not Found**, desacoplando la lógica de negocio de los códigos de estado del protocolo HTTP.

### 2.3. Aislamiento de Pruebas: `@DataJpaTest` y `@WebMvcTest`
Para evitar el uso indiscriminado de `@SpringBootTest` (que carga el contexto completo de Spring, consume memoria y ralentiza las suites de test), se aplicó la técnica de **Test Slicing (corte transversal)**:
1. **`@DataJpaTest` para la persistencia:**
   - Carga únicamente las entidades JPA y los repositorios Spring Data.
   - Configura un `TestEntityManager` para manipular directamente el estado de H2.
   - Aplica transaccionalidad automática con rollback al finalizar cada test, asegurando pruebas de consultas rápidas y atómicas.
2. **`@WebMvcTest` para la capa controladora:**
   - Carga únicamente la infraestructura Web MVC (filtros, serializadores Jackson y controladores).
   - Aísla la lógica de negocio simulando el servicio mediante `@MockBean`.
   - Permite validar códigos de estado (200, 201, 400, 404), cabeceras y estructura JSON mediante `MockMvc` sin necesidad de levantar un servidor Tomcat real ni abrir sockets de red.
3. **`@ExtendWith(MockitoExtension.class)` para la capa de servicio:**
   - Pruebas unitarias puras en milisegundos sin sobrecarga del contenedor de Spring.

### 2.4. Justificación de Exclusión de Selenium
**Selenium NO fue utilizado en este proyecto** debido a la naturaleza de su arquitectura:
- El proyecto `catalogo-productos` es una **API REST pura (Headless / Backend)** que consume y produce exclusivamente datos serializados en JSON. No cuenta con plantillas HTML del lado del servidor (como Thymeleaf, JSP o Velocity) ni vistas en navegador.
- Selenium es un framework especializado en la automatización de navegadores web (interacción con el DOM, renderizado gráfico de estilos CSS y eventos de usuario).
- Intentar probar una API REST con Selenium introduce overhead innecesario (descarga de webdrivers, apertura de procesos de navegador, fragilidad por timeouts de red).
- Para APIs RESTful, el estándar de la industria es utilizar **MockMvc** para pruebas a nivel de código y **Postman / Newman** para pruebas de contrato HTTP en vivo.

---

## 3. Cobertura de Código con JaCoCo

El proyecto tiene integrado el plugin `jacoco-maven-plugin` (versión 0.8.11), configurado en el archivo `pom.xml`:
- **Fase de ejecución:** `verify`.
- **Goals activados:** `prepare-agent`, `report` y `check`.
- **Regla de calidad:** `COVEREDRATIO` en métrica `LINE` >= **0.70 (70%)**.
- **Exclusiones de análisis:**
  - `**/*Application.class` (Clase de arranque del framework).
  - `**/model/**` (POJOs y entidades de dominio con getters/setters estándar).

### Ejecutar y verificar cobertura:
Desde la raíz del subproyecto `catalogo-productos`:
```bash
mvn clean verify
```
Si la cobertura de líneas es inferior al 70%, el proceso de compilación fallará automáticamente.

### Consulta del reporte HTML:
Una vez finalizada la ejecución, el reporte detallado se encuentra disponible en:
```text
catalogo-productos/target/site/jacoco/index.html
```

---

## 4. Guía de Ejecución Local

### Prerrequisitos
- JDK 17 o superior instalado y configurado en el `PATH`.
- Apache Maven 3.8 o superior.
- Node.js y Newman (opcional, para ejecución CLI de Postman).

### Levantar la Aplicación
```bash
cd catalogo-productos
mvn spring-boot:run
```
La aplicación iniciará en el puerto `8080`.

### Consola de Base de Datos H2
Con la aplicación en ejecución, se puede acceder a la consola web interactiva:
- **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL:** `jdbc:h2:mem:catalogodb`
- **User Name:** `sa`
- **Password:** *(dejar vacío)*

---

## 5. Pruebas de API con Postman y Newman

En el directorio `postman/` se suministran los artefactos de prueba:
1. `postman/ColeccionCatalogo.json`: Colección v2.1 con 4 peticiones y scripts `pm.test`.
2. `postman/env-local.json`: Archivo de variables de entorno (`baseUrl=http://localhost:8080`).

### Peticiones Incluidas:
| # | Método | Endpoint | Descripción | Validación `pm.test` |
|---|--------|----------|-------------|----------------------|
| 1 | POST | `/api/productos` | Crea un producto nuevo | Status 201, ID generado, guarda variable `productoId` |
| 2 | GET | `/api/productos/{{productoId}}` | Consulta el producto por ID | Status 200, coincide ID y campos |
| 3 | GET | `/api/productos?categoria=Perifericos` | Filtra productos por categoría | Status 200, respuesta array con categoría correcta |
| 4 | GET | `/api/productos/99999` | Consulta producto inexistente | Status 404, payload contiene mensaje de error |

### Ejecución Automatizada con Newman:
Con la aplicación Spring Boot corriendo en el puerto 8080:
```bash
newman run postman/ColeccionCatalogo.json -e postman/env-local.json
```

---

## 6. Estructura del Repositorio

```text
Plata_Post1_U10/
├── .gitignore
├── README.md
├── script_git.sh
├── capturas/
│   └── .gitkeep
├── postman/
│   ├── ColeccionCatalogo.json
│   └── env-local.json
└── catalogo-productos/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/universidad/catalogo/
        │   │   ├── CatalogoApplication.java
        │   │   ├── controller/
        │   │   │   └── ProductoController.java
        │   │   ├── model/
        │   │   │   └── Producto.java
        │   │   ├── repository/
        │   │   │   └── ProductoRepository.java
        │   │   └── service/
        │   │       └── ProductoService.java
        │   └── resources/
        │       └── application.properties
        └── test/
            └── java/com/universidad/catalogo/
                ├── controller/
                │   └── ProductoControllerTest.java
                ├── repository/
                │   └── ProductoRepositoryTest.java
                └── service/
```

---

## 7. Evidencias de Ejecución y Capturas

El detalle paso a paso de todas las pruebas ejecutadas, logs de consola, reportes de cobertura JaCoCo al 100% y capturas de pantalla de Postman se encuentra documentado en:

📄 **[Ver Documento Completo de Evidencias](capturas/EVIDENCIAS_EJECUCION.md)**

### Resumen de Evidencias Incluidas:
1. **Compilación y Tests:** `mvn clean verify` con 23 tests pasando y 0 fallos.
2. **Cobertura JaCoCo:** 100% de cobertura en líneas y ramas (superando el 70% requerido).
3. **Arranque:** Servidor Spring Boot en puerto 8080 con base H2 en memoria.
4. **POST `/api/productos`:** Creación exitosa (HTTP 201 Created).
5. **GET `/api/productos/{id}`:** Consulta por identificador (HTTP 200 OK).
6. **GET `/api/productos?categoria=`:** Filtrado de productos por categoría (HTTP 200 OK).
7. **GET `/api/productos/99999`:** Manejo de entidad no encontrada (HTTP 404 Not Found).
8. **POST con precio negativo:** Manejo de validación de negocio (HTTP 400 Bad Request).
9. **Newman CLI:** Ejecución automatizada con 10/10 assertions exitosas.
10. **Git Log:** Historial de commits semánticos y estructurados.

