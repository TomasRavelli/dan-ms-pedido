package dan.tp2021.pedidos.services;

	import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import dan.tp2021.pedidos.dao.PedidoRepositoryInMemory;
import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.EstadoPedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.dto.ObraDTO;

@Service
public class PedidoServiceImpl implements PedidoService {

	@Autowired
	PedidoRepositoryInMemory pedidoRepositoryInMemory;
	
	@Autowired
	ClienteService clienteServiceImpl;
	
	@Autowired
	BancoService bancoServiceImpl;

	@Override
	public ResponseEntity<Pedido> savePedido(Pedido p) throws RuntimeException {

		EstadoPedido estadoPedido = new EstadoPedido();
		
		ResponseEntity<ClienteDTO> clienteBuscadoByObra = clienteServiceImpl.getClienteByObra(p);

		if(clienteBuscadoByObra.getStatusCode().equals(HttpStatus.OK)) {
		
			ClienteDTO clienteDTO = clienteBuscadoByObra.getBody();
		
			double sumaCostosProductos = 0;
			boolean stockDisponible = true;
			
			for(DetallePedido dp: p.getDetalle()) {
				
				sumaCostosProductos += dp.getPrecio();
				//Aca se deberia usar el materialService creo. COMENTARIO DE PERU.
				//Eso estaba implementado por el profe, si quieren lo hacemos como el. COMENTARIO TOMAS.
//				if(materialService.stockDisponible(dp.getProducto()) <= 0 && stockDisponible){
//					stockDisponible = false;
//				}
				if(dp.getProducto().getStockActual() <= 0 && stockDisponible) {
					
					stockDisponible = false;
				
				}
			
			}
			
			boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(), superaDescubierto = sumaCostosProductos>clienteDTO.getMaxCuentaOnline();
			boolean condicionC = !superaDescubierto && bancoServiceImpl.verificarSituacionCliente(clienteDTO); //TODO ver como tratamos el getHabilitadoOnline
					
			if (!esDeudor || condicionC) {
				
				if(stockDisponible) {
					
					estadoPedido.setEstado("ACEPTADO");
					
					try{	
						p.setEstado(estadoPedido);
						return ResponseEntity.ok(pedidoRepositoryInMemory.save(p));
					}catch (Exception e) {
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
					}
			
				}
				else {
					
					estadoPedido.setEstado("PENDIENTE");
					
					try{	
						p.setEstado(estadoPedido);
						return ResponseEntity.ok(pedidoRepositoryInMemory.save(p));
					}catch (Exception e) {
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
					}
			
				}
			}
		
			else {
				throw new RuntimeException("Error. El cliente no cumple con las condiciones para adquirir el pedido");
			}
		
		}
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		
	}
}
