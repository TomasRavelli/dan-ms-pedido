package dan.tp2021.pedidos.services;

import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;

public interface ClienteService {

	public static class ClienteException extends Exception { ClienteException(String message){super(message);}}
	
	public ClienteDTO getClienteByObra(Pedido p) throws ClienteException;
	
}
