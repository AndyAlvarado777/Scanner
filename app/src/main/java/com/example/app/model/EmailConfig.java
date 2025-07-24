// com.example.app.model/EmailConfig.java
package com.example.app.model;

public class EmailConfig {
    private long id; // Para el _ID de la base de datos
    private String remitenteEmail;
    private String appPassword;
    private String destinatariosEmails;

    public EmailConfig() {
        // Constructor vacío
    }

    public EmailConfig(String remitenteEmail, String appPassword, String destinatariosEmails) {
        this.remitenteEmail = remitenteEmail;
        this.appPassword = appPassword;
        this.destinatariosEmails = destinatariosEmails;
    }

    // Getters y Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRemitenteEmail() {
        return remitenteEmail;
    }

    public void setRemitenteEmail(String remitenteEmail) {
        this.remitenteEmail = remitenteEmail;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public void setAppPassword(String appPassword) {
        this.appPassword = appPassword;
    }

    public String getDestinatariosEmails() {
        return destinatariosEmails;
    }

    public void setDestinatariosEmails(String destinatariosEmails) {
        this.destinatariosEmails = destinatariosEmails;
    }
}