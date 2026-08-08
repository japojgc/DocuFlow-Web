/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.docuflow.util;

public class DocumentValidator {
    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "png", "jpg", "jpeg"};

    public static boolean isValidExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
