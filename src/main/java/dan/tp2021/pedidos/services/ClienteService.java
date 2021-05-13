package dan.tp2021.pedidos.services;

import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;

public interface ClienteService {

	
	public ClienteDTO getClienteByObra(Pedido p) throws ClienteException;
	
}
