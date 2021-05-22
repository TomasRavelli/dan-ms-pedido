package dan.tp2021.pedidos.domain;

import java.time.Instant;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.springframework.data.annotation.ReadOnlyProperty;

@Entity
public class Pedido {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//	@Column(name = "ID_PEDIDO")
	private Integer id;
	private Instant fechaPedido;
	
	@ManyToOne
//	@JoinColumn(name = "ID_OBRA")
	private Obra obra; //TODO verlo. Creo que debemos ir al servicio de USUARIOS y buscar si existe una obra con este ID, en ese caso, la guardamos en esta BD antes de guardar el Pedido, solo con el proposito de tener la relacion. Otra opcion seria que cada cambio que ocurra en la BD de datos del servicio USUARIOS se refleje en esta BD.
	
	@OneToMany(cascade = CascadeType.ALL)
//	@JoinColumn(name = "ID_PEDIDO")
	private List<DetallePedido> detalle;
	
	@ManyToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "ID_ESTADO_PEDIDO")
	private EstadoPedido estado; //Lo podriamoss tener precargados en la BD.
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Instant getFechaPedido() {
		return fechaPedido;
	}
	public void setFechaPedido(Instant fechaPedido) {
		this.fechaPedido = fechaPedido;
	}
	public Obra getObra() {
		return obra;
	}
	public void setObra(Obra obra) {
		this.obra = obra;
	}
	public List<DetallePedido> getDetalle() {
		return detalle;
	}
	public void setDetalle(List<DetallePedido> detalle) {
		this.detalle = detalle;
	}
	public EstadoPedido getEstado() {
		return estado;
	}
	public void setEstado(EstadoPedido estado) {
		this.estado = estado;
	}

	
}
