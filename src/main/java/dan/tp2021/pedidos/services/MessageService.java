package dan.tp2021.pedidos.services;

import dan.tp2021.pedidos.domain.Pedido;

public interface MessageService{
	void sendMessageToProductos(Pedido p);
}