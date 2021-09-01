package dan.tp2021.pedidos.services;

import java.util.List;

import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.EstadoPedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.exceptions.cliente.ClienteBadRequestException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteNoHabilitadoException;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;
import dan.tp2021.pedidos.exceptions.pedido.*;



public interface PedidoService {

	public Pedido savePedido(Pedido p) throws ClienteNoHabilitadoException, ClienteException, ObraNoEncontradaException, ClienteBadRequestException;

	public Pedido addItem(Integer idPedido, DetallePedido detalle) throws dan.tp2021.pedidos.exceptions.pedido.PedidoNoEncontradoException;

	public Pedido updatePedido(Integer idPedido, Pedido nuevoPedido) throws PedidoNoEncontradoException, ClienteException, ObraNoEncontradaException, ClienteBadRequestException, ClienteNoHabilitadoException;

	public Pedido deletePedidoById(Integer idPedido) throws PedidoNoEncontradoException;

	public Pedido deleteDetallePedidoPedidoById(Integer idPedido, Integer idDetalle) throws DetallePedidoNoEncontradoException, PedidoNoEncontradoException;

	public Pedido getPedidoByID(Integer idPedido) throws PedidoNoEncontradoException;

	public List<Pedido> getPedidoByIdObra(Integer idObra) throws PedidoNoEncontradoException;

	public List<Pedido> getPedidosByClientParams(Integer idCliente, String cuitCliente, String estadoPedido) throws PedidoNoEncontradoException, ObraNoEncontradaException;

	public DetallePedido getDetallePedidoById(Integer idPedido, Integer id)throws DetallePedidoNoEncontradoException, PedidoNoEncontradoException;

	public EstadoPedido getEstadoPedido(String string);
}
