package dan.tp2021.pedidos.services;

import dan.tp2021.pedidos.domain.EstadoPedido;

public interface EstadoPedidoService {
	public EstadoPedido findByEstado(String estado);
}
