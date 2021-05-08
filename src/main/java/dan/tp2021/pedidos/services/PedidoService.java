package dan.tp2021.pedidos.services;

import dan.tp2021.pedidos.domain.Pedido;


public interface PedidoService {

	public static class ClienteDeudorException extends Exception{ ClienteDeudorException(String message){super(message);}}
	public static class ClienteNoEncontradoException extends Exception{ ClienteNoEncontradoException(String message){super(message);}}

	public Pedido savePedido(Pedido p) throws ClienteDeudorException, ClienteNoEncontradoException;
}
