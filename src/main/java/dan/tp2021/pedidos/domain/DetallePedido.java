package dan.tp2021.pedidos.domain;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DetallePedido {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private Integer cantidad;
	private Double precio;
	
	@ManyToOne
	@JoinColumn(name = "producto_id", foreignKey = @ForeignKey(name = "FK_producto_detalle_pedido")) //Definido exactamente igual que en dan-ms-productos
	private Producto producto; //TODO verlo. Creo que debemos ir al servicio de MATERIAL y buscar si existe un PRODUCTO con este ID, en ese caso, lo guardamos en esta BD antes de guardar el DetallePedido, solo con el proposito de tener la relacion. Otra opcion seria que cada cambio que ocurra en la BD de datos del servicio MATERIAL se refleje en esta BD ymantener todos los materiales en ambas BD, lo cual no tiene sentido.
	
	
	public DetallePedido(){
		
	}
	
	
	public DetallePedido(Producto producto, Integer cantidad, Double precio) {
		super();
		this.producto = producto;
		this.cantidad = cantidad;
		this.precio = precio;
	}


	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Producto getProducto() {
		return producto;
	}
	public void setProducto(Producto producto) {
		this.producto = producto;
	}
	public Integer getCantidad() {
		return cantidad;
	}
	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}
	public Double getPrecio() {
		return precio;
	}
	public void setPrecio(Double precio) {
		this.precio = precio;
	}
	
}
