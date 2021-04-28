package dan.tp2021.pedidos.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import dan.tp2021.pedidos.domain.Pedido;

@Service
public interface PedidoService {

	public ResponseEntity<Pedido> savePedido(Pedido p) throws Exception;
}
