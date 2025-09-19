// En: com.example.app.model/Producto.java
package com.example.app.model;

public class Producto {
    private String codigoProveedor;
    private String codigoInterno;

    // Constructores, getters y setters
    public Producto() {}

    public Producto(String codigoProveedor, String codigoInterno) {
        this.codigoProveedor = codigoProveedor;
        this.codigoInterno = codigoInterno;
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }

    public void setCodigoProveedor(String codigoProveedor) {
        this.codigoProveedor = codigoProveedor;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }
}