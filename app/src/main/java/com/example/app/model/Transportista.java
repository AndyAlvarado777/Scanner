package com.example.app.model;

import java.io.Serializable;

public class Transportista implements Serializable {
    private String codigoTransportista;
    private String codigoEmpresa;
    private String nombreTransportista;

    // Constructor vacío
    public Transportista() {}

    public Transportista(String codigoTransportista, String codigoEmpresa, String nombreTransportista) {
        this.codigoTransportista = codigoTransportista;
        this.codigoEmpresa = codigoEmpresa;
        this.nombreTransportista = nombreTransportista;
    }

    // Getters y Setters
    public String getCodigoTransportista() {
        return codigoTransportista;
    }

    public void setCodigoTransportista(String codigoTransportista) {
        this.codigoTransportista = codigoTransportista;
    }

    public String getCodigoEmpresa() {
        return codigoEmpresa;
    }

    public void setCodigoEmpresa(String codigoEmpresa) {
        this.codigoEmpresa = codigoEmpresa;
    }

    public String getNombreTransportista() {
        return nombreTransportista;
    }

    public void setNombreTransportista(String nombreTransportista) {
        this.nombreTransportista = nombreTransportista;
    }

    @Override
    public String toString() {
        return nombreTransportista + " (" + codigoTransportista + ")";
    }
}