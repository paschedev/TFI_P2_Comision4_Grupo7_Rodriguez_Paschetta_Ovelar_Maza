package tfi.entities;

import java.time.LocalDate;

/**
 * Entidad que representa un envío.
 * Corresponde a la tabla 'envios' en la base de datos.
 */
public class Envio {
    
    private Long id;
    private boolean eliminado;
    private String tracking;
    private EmpresaEnvio empresa;
    private TipoEnvio tipo;
    private double costo;
    private LocalDate fechaDespacho;
    private LocalDate fechaEstimada;
    private EstadoEnvio estado;
    
    // Constructores
    public Envio() {
        this.eliminado = false;
        this.estado = EstadoEnvio.EN_PREPARACION;
    }
    
    public Envio(String tracking, EmpresaEnvio empresa, TipoEnvio tipo, double costo) {
        this();
        this.tracking = tracking;
        this.empresa = empresa;
        this.tipo = tipo;
        this.costo = costo;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public boolean isEliminado() {
        return eliminado;
    }
    
    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
    
    public String getTracking() {
        return tracking;
    }
    
    public void setTracking(String tracking) {
        this.tracking = tracking;
    }
    
    public EmpresaEnvio getEmpresa() {
        return empresa;
    }
    
    public void setEmpresa(EmpresaEnvio empresa) {
        this.empresa = empresa;
    }
    
    public TipoEnvio getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoEnvio tipo) {
        this.tipo = tipo;
    }
    
    public double getCosto() {
        return costo;
    }
    
    public void setCosto(double costo) {
        this.costo = costo;
    }
    
    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }
    
    public void setFechaDespacho(LocalDate fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }
    
    public LocalDate getFechaEstimada() {
        return fechaEstimada;
    }
    
    public void setFechaEstimada(LocalDate fechaEstimada) {
        this.fechaEstimada = fechaEstimada;
    }
    
    public EstadoEnvio getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoEnvio estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "Envio{" +
                "id=" + id +
                ", tracking='" + tracking + '\'' +
                ", empresa=" + empresa +
                ", tipo=" + tipo +
                ", costo=" + costo +
                ", fechaDespacho=" + fechaDespacho +
                ", fechaEstimada=" + fechaEstimada +
                ", estado=" + estado +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Envio envio = (Envio) o;
        return id != null && id.equals(envio.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    // Enums
    public enum EmpresaEnvio {
        ANDREANI, OCA, CORREO_ARG
    }
    
    public enum TipoEnvio {
        ESTANDAR, EXPRES
    }
    
    public enum EstadoEnvio {
        EN_PREPARACION, EN_TRANSITO, ENTREGADO
    }
}
