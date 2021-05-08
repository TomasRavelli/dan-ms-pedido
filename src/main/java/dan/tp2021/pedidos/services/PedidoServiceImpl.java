package dan.tp2021.pedidos.services;

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
public class PedidoServiceImpl implements PedidoService{
		
	@Autowired
	PedidoRepositoryInMemory pedidoRepositoryInMemory;
	
	@Autowired
	BancoService bancoServiceImpl;

	@Override
	public Pedido savePedido(Pedido p) throws ClienteDeudorException, ClienteNoEncontradoException {
		
		EstadoPedido estadoPedido = new EstadoPedido();

		//Por que ese metodo no retorna directamente un cliente? (o un clienteDTO)
		ClienteDTO clienteDTO = buscarClienteEnServicioUsuario(p);

		//Si llego a esta línea es porque tengo un cliete, porque sino se lanza una excepción que corta todo.

		double sumaCostosProductos = 0;
		boolean stockDisponible = true;

		for (DetallePedido dp : p.getDetalle()) {

			sumaCostosProductos += dp.getPrecio();

			if (dp.getProducto().getStockActual() <= 0 && stockDisponible) {

				stockDisponible = false;
			}
		}

		boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(), superaDescubierto = sumaCostosProductos > clienteDTO.getMaxCuentaOnline();
		boolean condicionC = superaDescubierto && bancoServiceImpl.verificarSituacionCliente(clienteDTO); //TODO ver como tratamos el getHabilitadoOnline

		if (!esDeudor || condicionC) {

			if (stockDisponible) {

				estadoPedido.setEstado("ACEPTADO");

				p.setEstado(estadoPedido);

			} else {

				estadoPedido.setEstado("PENDIENTE");

				p.setEstado(estadoPedido);

			}
			//Este métod podría tirar una excepción en otra implementación del repositori, si queremos.
			//Pero creo que no haría falta capturarla acá, la podemos capturar  en el Controller y decidir qué hacer ahí.
			return pedidoRepositoryInMemory.save(p); //El InMemoryRepository no lanza ninguna exepción.
		} else {
			//Lanzo una excepción que me da información sobre el error.
			throw new ClienteDeudorException("Error. El cliente no cumple con las condiciones para adquirir el pedido");
		}

	}
	
	private ClienteDTO buscarClienteEnServicioUsuario(Pedido p) throws ClienteNoEncontradoException {
		
		//Buscar en el servicio Usuario la obra, para encontrar a que cliente pertenece.
		WebClient client = WebClient.create("http://localhost:8080/api/obra/"+p.getObra().getId());
		
		ResponseEntity<ObraDTO> response = client.get()
				 .accept(MediaType.APPLICATION_JSON)
				 .retrieve()
				 .toEntity(ObraDTO.class)
				 .block();
		
		if(response.getStatusCode().equals(HttpStatus.OK)){
			
			//TODO ver si se puede arreglar para que no entre dos veces a la API de cliente. Esto sucede porque el JSON no tiene un cliente asociado.
			
			client = WebClient.create("http://localhost:8080/api/cliente/"+response.getBody().getIdCliente()); //Buscar los datos del cliente en el servicio de usuarios.
			
			
			ResponseEntity<ClienteDTO> clienteResponse = client.get()
					 .accept(MediaType.APPLICATION_JSON)
					 .retrieve()
					 .toEntity(ClienteDTO.class)
					 .block();
			
			if(clienteResponse.getStatusCode().equals(HttpStatus.OK)) {
				return clienteResponse.getBody();
			}
		}

		throw new ClienteNoEncontradoException("Error. No se puedo encontrar al cliente.");
			
	}

}
