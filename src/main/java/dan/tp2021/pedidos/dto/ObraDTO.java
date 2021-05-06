package dan.tp2021.pedidos.dto;

public class ObraDTO {
	
	private Integer id;
	private Integer idCliente;
	public ObraDTO() {
	}

	public ObraDTO(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}
}
