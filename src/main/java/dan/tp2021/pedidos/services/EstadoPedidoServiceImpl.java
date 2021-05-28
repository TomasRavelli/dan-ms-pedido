package dan.tp2021.pedidos.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dan.tp2021.pedidos.dao.EstadoPedidoRepository;
import dan.tp2021.pedidos.domain.EstadoPedido;

@Service
public class EstadoPedidoServiceImpl implements EstadoPedidoService {

	@Autowired
	EstadoPedidoRepository estadoPedidoRepository;
	
	@Override
	public EstadoPedido findByEstado(String estado) {
		return estadoPedidoRepository.findByEstado(estado);
	}

}
