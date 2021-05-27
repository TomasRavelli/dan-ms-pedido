package dan.tp2021.pedidos.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class EstadoPedido {
	//TODO podriamos tenerlo precargado en la BD
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//	@Column(name = "ID_ESTADO_PEDIDO")
	private Integer id;
	private String estado;
	
	public EstadoPedido() {
		
	}
	
	public EstadoPedido(Integer id, String estado) {
		super();
		this.id = id;
		this.estado = estado;
	}
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}

}
