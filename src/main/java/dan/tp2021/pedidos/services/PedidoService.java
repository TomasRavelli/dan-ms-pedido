package dan.tp2021.pedidos.services;

import java.util.List;

import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.services.ObraService.ObraNoEncontradaException;
import dan.tp2021.pedidos.services.PedidoService.DetallePedidoNoEncontradoException;
import dan.tp2021.pedidos.services.PedidoService.PedidoNoEncontradoException;


public interface PedidoService {

	public static class ClienteNoHabilitadoException extends Exception { ClienteNoHabilitadoException(String message){super(message);}}
	public static class PedidoNoEncontradoException extends Exception {PedidoNoEncontradoException(String msg) {super (msg);}}
	public static class DetallePedidoNoEncontradoException extends Exception {DetallePedidoNoEncontradoException(String msg) {super (msg);}}

	public Pedido savePedido(Pedido p) throws ClienteNoHabilitadoException, ClienteService.ClienteException;

	public Pedido addItem(Integer idPedido, DetallePedido detalle) throws PedidoNoEncontradoException;

	public Pedido updatePedido(Integer idPedido, Pedido nuevoPedido) throws PedidoNoEncontradoException;

	public Pedido deletePedidoById(Integer idPedido) throws PedidoNoEncontradoException;

	public Pedido deleteDetallePedidoPedidoById(Integer idPedido, Integer idDetalle) throws DetallePedidoNoEncontradoException, PedidoNoEncontradoException;

	public Pedido getPedidoByID(Integer idPedido) throws PedidoNoEncontradoException;

	public List<Pedido> getPedidoByIdObra(Integer idObra) throws PedidoNoEncontradoException;

	public List<Pedido> getPedidosByClientParams(Integer idCliente, String cuitCliente) throws PedidoNoEncontradoException, ObraNoEncontradaException;

	public DetallePedido getDetallePedidoById(Integer idPedido, Integer id)throws DetallePedidoNoEncontradoException, PedidoNoEncontradoException;
}
