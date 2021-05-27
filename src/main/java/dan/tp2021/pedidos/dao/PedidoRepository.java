package dan.tp2021.pedidos.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dan.tp2021.pedidos.domain.Pedido;

public interface PedidoRepository  extends JpaRepository<Pedido, Integer> {
	public List<Pedido> findByEstadoEstado(String estado);
}
