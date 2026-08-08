
# DocuFlow Web - Sistema de Gestión Documental

## Tabla de Contenidos
1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Requerimientos del Sistema](#requerimientos-del-sistema)
3. [Instalación y Despliegue](#instalación-y-despliegue)
4. [Configuración](#configuración)
5. [Guía de Uso](#guía-de-uso)
6. [Guía de Contribución](#guía-de-contribución)
7. [Roadmap](#roadmap)

---

## Resumen Ejecutivo

* **Descripción:** DocuFlow Web es una solución empresarial basada en Jakarta EE / JavaEE diseñada para centralizar, validar y gestionar el flujo de documentos organizacionales.
* **Problema Identificado:** Ineficiencia en la validación manual de formatos de archivos subidos por colaboradores, generando riesgos de seguridad y almacenamiento inadecuado.
* **Solución:** Plataforma web con autenticación basada en roles, filtrado automático de extensiones de archivos y pipeline automatizado de Integración Continua (CI/CD).
* **Arquitectura:** Modelo Vista Controlador (MVC) mediante Servlets, JSP y Bootstrap 5, desplegado sobre el servidor de aplicaciones Eclipse GlassFish.

---

## Requerimientos del Sistema

* **Servidor de Aplicaciones:** Eclipse GlassFish Server (v5, v6 o v7).
* **Versión de Java:** JDK 17 LTS (o JDK 11 según la versión del runtime de GlassFish).
* **Tecnologías:** Jakarta EE / JavaEE (Servlets, JSP).
* **Gestor de Dependencias:** Apache Maven 3.8+.
* **Librerías Adicionales:** JUnit 5.9.2 (para pruebas unitarias), Bootstrap 5 (CDN UI).

---

## Instalación y Despliegue

### 1. Ambiente de Desarrollo
1. Clonar el repositorio: `git clone https://github.com/japojgc/DocuFlow-Web`
2. Abrir NetBeans y seleccionar **File > Open Project**.
3. Importar la carpeta del proyecto `docuflow-web`.
4. Ejecutar `Clean and Build` en NetBeans para descargar dependencias de Maven.

### 2. Ejecución de Pruebas Manuales
Para ejecutar la suite de pruebas unitarias desde la terminal:
```bash
mvn test