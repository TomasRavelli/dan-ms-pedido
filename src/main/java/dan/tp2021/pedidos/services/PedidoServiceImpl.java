package dan.tp2021.pedidos.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import dan.tp2021.pedidos.dao.PedidoRepositoryInMemory;
import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.EstadoPedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.dto.ObraDTO;

public class PedidoServiceImpl implements PedidoService{
		
	@Autowired
	PedidoRepositoryInMemory pedidoRepositoryInMemory;

	@Override
	public ResponseEntity<Pedido> savePedido(Pedido p) throws Exception {
		
		EstadoPedido estadoPedido = new EstadoPedido();
		
		WebClient client = WebClient.create("http://localhost:8080/api/obra/"+p.getObra().getId());
		
		ResponseEntity<ObraDTO> response = client.get()
				 .accept(MediaType.APPLICATION_JSON)
				 .retrieve()
				 .toEntity(ObraDTO.class)
				 .block();
		
		if(response.getStatusCode().equals(HttpStatus.OK)){
			
			ClienteDTO clienteDTO = response.getBody().getClienteDTO();
	
			double sumaCostosProductos = 0;
			boolean stockDisponible = true;
			
			for(DetallePedido dp: p.getDetalle()) {
				
				sumaCostosProductos += dp.getPrecio();
				
				if(dp.getProducto().getStockActual() <= 0 && stockDisponible) {
				
					stockDisponible = false;
				}
			}
			
			boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(), superaDescubierto = sumaCostosProductos>clienteDTO.getMaxCuentaOnline();
			boolean condicionC = superaDescubierto && clienteDTO.getHabilitadoOnline(); //TODO ver como tratamos el getHabilitadoOnline
			
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
				throw new Exception("Error. El cliente no cumple con las condiciones para adquirir el pedido");
			}
		
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
	}
	

}
