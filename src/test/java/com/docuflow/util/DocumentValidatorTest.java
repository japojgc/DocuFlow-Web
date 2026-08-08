package com.docuflow.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentValidatorTest {

    @Test
    @DisplayName("Debe validar correctamente un archivo PDF permitido")
    public void testValidPdfExtension() {
        String filename = "solicitud_presupuesto.pdf";
        boolean isValid = DocumentValidator.isValidExtension(filename);
        assertTrue(isValid, "El archivo PDF debería ser aceptado");
    }

    @Test
    @DisplayName("Debe rechazar archivos con extensiones no permitidas")
    public void testInvalidExtension() {
        String filename = "script_malicioso.exe";
        boolean isValid = DocumentValidator.isValidExtension(filename);
        assertFalse(isValid, "Los archivos .exe deben ser rechazados por seguridad");
    }

    @Test
    @DisplayName("Debe rechazar nombres de archivo nulos o sin extensión")
    public void testNullOrNoExtension() {
        assertFalse(DocumentValidator.isValidExtension(null));
        assertFalse(DocumentValidator.isValidExtension("archivo_sin_extension"));
    }
}