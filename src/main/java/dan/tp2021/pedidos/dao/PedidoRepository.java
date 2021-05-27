package dan.tp2021.pedidos.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import dan.tp2021.pedidos.domain.Pedido;

public interface PedidoRepository  extends JpaRepository<Pedido, Integer> {
	
}
