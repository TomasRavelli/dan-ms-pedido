package dan.tp2021.pedidos.services;

import dan.tp2021.pedidos.domain.Pedido;


public interface PedidoService {

	public static class ClienteNoHabilitadoException extends Exception { ClienteNoHabilitadoException(String message){super(message);}}

	public Pedido savePedido(Pedido p) throws ClienteNoHabilitadoException, ClienteService.ClienteException;
}
