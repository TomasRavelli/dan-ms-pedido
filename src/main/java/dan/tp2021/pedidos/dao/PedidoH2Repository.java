package dan.tp2021.pedidos.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dan.tp2021.pedidos.domain.Pedido;

@Repository
public interface PedidoH2Repository extends JpaRepository<Pedido, Integer> {


}
