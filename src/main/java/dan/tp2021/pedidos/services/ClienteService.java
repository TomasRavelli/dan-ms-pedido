package dan.tp2021.pedidos.services;

import org.springframework.http.ResponseEntity;

import dan.tp2021.pedidos.domain.Obra;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.domain.Producto;
import dan.tp2021.pedidos.dto.ClienteDTO;

public interface ClienteService {
	
	public ResponseEntity<ClienteDTO> getClienteByObra(Pedido p);
}
