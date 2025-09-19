package com.example.app.model;

import java.io.Serializable;

public class Despachador implements Serializable {
    private String codigoDespachador;
    private String codigoEmpresa;
    private String nombreDespachador;

    // Constructor vacío
    public Despachador() {}

    public Despachador(String codigoDespachador, String codigoEmpresa, String nombreDespachador) {
        this.codigoDespachador = codigoDespachador;
        this.codigoEmpresa = codigoEmpresa;
        this.nombreDespachador = nombreDespachador;
    }

    // Getters y Setters
    public String getCodigoDespachador() {
        return codigoDespachador;
    }

    public void setCodigoDespachador(String codigoDespachador) {
        this.codigoDespachador = codigoDespachador;
    }

    public String getCodigoEmpresa() {
        return codigoEmpresa;
    }

    public void setCodigoEmpresa(String codigoEmpresa) {
        this.codigoEmpresa = codigoEmpresa;
    }

    public String getNombreDespachador() {
        return nombreDespachador;
    }

    public void setNombreDespachador(String nombreDespachador) {
        this.nombreDespachador = nombreDespachador;
    }
}
