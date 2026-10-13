#!/usr/bin/env bash
# ==============================================================================
# Script de Inicializacion y Commits Estructurados para el Laboratorio U10
# Repositorio: https://github.com/DevvBP/PLATA_Post1_U10
# ==============================================================================

set -e

REPO_URL="https://github.com/DevvBP/PLATA_Post1_U10.git"

echo "=== 1. Creando directorios requeridos ==="
mkdir -p capturas
mkdir -p postman

echo "=== 2. Inicializando y configurando Git ==="
if [ ! -d ".git" ]; then
    git init
    git branch -M main
fi

# Configurar remote origin si no existe
if git remote | grep -q "origin"; then
    git remote set-url origin "$REPO_URL"
else
    git remote add origin "$REPO_URL"
fi

echo "=== 3. Ejecutando commits en orden logico ==="

# ------------------------------------------------------------------------------
# PARTE 1: Logica y Persistencia con Pruebas
# ------------------------------------------------------------------------------

# Commit 1: Proyecto base, configuracion y entidad de dominio
git add .gitignore
git add catalogo-productos/pom.xml
git add catalogo-productos/src/main/resources/application.properties
git add catalogo-productos/src/main/java/com/universidad/catalogo/CatalogoApplication.java
git add catalogo-productos/src/main/java/com/universidad/catalogo/model/Producto.java
git commit -m "feat(core): configurar proyecto base spring boot y entidad Producto" || echo "Paso 1 ya commiteado"

# Commit 2: Capa de persistencia y pruebas JPA
git add catalogo-productos/src/main/java/com/universidad/catalogo/repository/ProductoRepository.java
git add catalogo-productos/src/test/java/com/universidad/catalogo/repository/ProductoRepositoryTest.java
git commit -m "feat(repository): implementar ProductoRepository y pruebas de persistencia con DataJpaTest" || echo "Paso 2 ya commiteado"

# Commit 3: Capa de servicio y pruebas unitarias Mockito
git add catalogo-productos/src/main/java/com/universidad/catalogo/service/ProductoService.java
git add catalogo-productos/src/test/java/com/universidad/catalogo/service/ProductoServiceTest.java
git commit -m "feat(service): implementar reglas de negocio en ProductoService y pruebas unitarias" || echo "Paso 3 ya commiteado"

# ------------------------------------------------------------------------------
# PARTE 2: Capa Web, Pruebas Web y Postman
# ------------------------------------------------------------------------------

# Commit 4: Controlador REST y manejo de excepciones
git add catalogo-productos/src/main/java/com/universidad/catalogo/controller/ProductoController.java
git commit -m "feat(controller): exponer endpoints REST y manejo de excepciones en ProductoController" || echo "Paso 4 ya commiteado"

# Commit 5: Pruebas Web MVC con MockMvc
git add catalogo-productos/src/test/java/com/universidad/catalogo/controller/ProductoControllerTest.java
git commit -m "test(controller): agregar pruebas web mvc con MockMvc para endpoints de productos" || echo "Paso 5 ya commiteado"

# Commit 6: Artefactos Postman, capturas, documentacion README y script
git add postman/
git add capturas/
git add README.md
git add script_git.sh
git commit -m "docs: agregar coleccion postman, entorno de pruebas y documentacion tecnica" || echo "Paso 6 ya commiteado"

echo "=== 4. Enviando cambios al repositorio remoto ==="
git push -u origin main --force

echo "=== Repositorio sincronizado exitosamente con $REPO_URL ==="
