package dan.tp2021.pedidos.dto;

public class ObraDTO {
	
	private Integer id;
	private ClienteDTO cliente;
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


	public ClienteDTO getCliente() {
		return cliente;
	}

	public void setCliente(ClienteDTO c) {
		this.cliente = c;
	}
}
