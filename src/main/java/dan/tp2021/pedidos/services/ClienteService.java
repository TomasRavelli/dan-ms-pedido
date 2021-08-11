package dan.tp2021.pedidos.services;

import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.exceptions.cliente.ClienteBadRequestException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;

public interface ClienteService {

	
	public ClienteDTO getClienteByObra(Pedido p) throws ClienteException, ObraNoEncontradaException, ClienteBadRequestException;
	
}
