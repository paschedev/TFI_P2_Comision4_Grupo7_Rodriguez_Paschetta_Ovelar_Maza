package tfi.entities;

import java.time.LocalDate;

/**
 * Entidad que representa un pedido.
 * Corresponde a la tabla 'pedidos' en la base de datos.
 * Mantiene una relación 1→1 unidireccional con Envio.
 */
public class Pedido {
    
    private Long id;
    private boolean eliminado;
    private String numero;
    private LocalDate fecha;
    private String clienteNombre;
    private double total;
    private EstadoPedido estado;
    private Envio envio; // Relación 1→1 unidireccional
    
    // Constructores
    public Pedido() {
        this.eliminado = false;
        this.estado = EstadoPedido.NUEVO;
    }
    
    public Pedido(String numero, LocalDate fecha, String clienteNombre, double total) {
        this();
        this.numero = numero;
        this.fecha = fecha;
        this.clienteNombre = clienteNombre;
        this.total = total;
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
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public String getClienteNombre() {
        return clienteNombre;
    }
    
    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public EstadoPedido getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }
    
    public Envio getEnvio() {
        return envio;
    }
    
    public void setEnvio(Envio envio) {
        this.envio = envio;
    }
    
    /**
     * Verifica si el pedido tiene un envío asociado.
     * @return true si tiene envío, false en caso contrario
     */
    public boolean tieneEnvio() {
        return envio != null;
    }
    
    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", fecha=" + fecha +
                ", clienteNombre='" + clienteNombre + '\'' +
                ", total=" + total +
                ", estado=" + estado +
                ", envio=" + (envio != null ? envio.getId() : "null") +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id != null && id.equals(pedido.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    // Enum
    public enum EstadoPedido {
        NUEVO, FACTURADO, ENVIADO
    }
}
