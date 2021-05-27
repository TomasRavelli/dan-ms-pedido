package dan.tp2021.pedidos.dao;

import org.springframework.stereotype.Repository;

import dan.tp2021.pedidos.domain.Pedido;
import frsf.isi.dan.InMemoryRepository;

@Deprecated
@Repository
public class PedidoRepositoryInMemory extends InMemoryRepository<Pedido>{

	@Override
	public Integer getId(Pedido entity) {
		return 	entity.getId();
	}

	@Override
	public void setId(Pedido entity, Integer id) {
		entity.setId(id);
		
	}

}
