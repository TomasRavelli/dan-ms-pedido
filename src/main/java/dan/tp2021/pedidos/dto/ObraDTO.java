package dan.tp2021.pedidos.dto;

public class ObraDTO {
	
	private Integer id;
	private ClienteDTO clienteDTO;
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

	public ClienteDTO getClienteDTO() {
		return clienteDTO;
	}

	public void setClienteDTO(ClienteDTO clienteDTO) {
		this.clienteDTO = clienteDTO;
	}
}
