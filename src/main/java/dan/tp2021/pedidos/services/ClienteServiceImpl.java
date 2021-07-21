package dan.tp2021.pedidos.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import dan.tp2021.pedidos.config.UsuarioRestProperties;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.dto.ObraDTO;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;

@Service
public class ClienteServiceImpl implements ClienteService {

	private static final Logger logger = LoggerFactory.getLogger(ClienteServiceImpl.class);

	@Autowired
	UsuarioRestProperties usuarioRestProperties;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	@Override
	public ClienteDTO getClienteByObra(Pedido p) throws ClienteException {

		// Buscar en el servicio Usuario la obra, para encontrar a que cliente
		// pertenece.

		String url = usuarioRestProperties.getUrl();
		logger.debug("Url de pedidos: " + url);

		WebClient client = WebClient.create(url + "/api");
		CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

		return circuitBreaker.run(() -> {
			ResponseEntity<ObraDTO> response = client.get().uri("/obra/" + p.getObra().getId())
					.accept(MediaType.APPLICATION_JSON).retrieve().toEntity(ObraDTO.class).block();
			logger.debug("Status response from api obra: " + response.getStatusCodeValue());
			if (response.getStatusCode().equals(HttpStatus.OK)) {

				// TODO ver si se puede arreglar para que no entre dos veces a la API de
				// cliente.
				// Esto sucede porque el JSON no tiene un cliente asociado.
				// Podemos hacer que se pueda buscar clietnes por id de obra...

				// Buscar los datos del cliente en el servicio de usuarios.

				logger.debug("ID obra buscada: " + response.getBody().getId());
				ObraDTO obra = response.getBody();

				ResponseEntity<ClienteDTO> clienteResponse = client.get().uri("/cliente/" + obra.getCliente().getId())
						.accept(MediaType.APPLICATION_JSON).retrieve().toEntity(ClienteDTO.class).block();

				if (clienteResponse.getStatusCode().equals(HttpStatus.OK)) {
					return clienteResponse.getBody();
				}
			}
			// TODO podríamos hacer que se lanzen distintas exceptiones según que error
			// recibimos de la API.
			// Por ahora para todos los errores lanzamos la misma excepción.
			//throw new ClienteException("Error al buscar al cliente");
			return defaultCliente();
		}, throwable -> defaultCliente());

		

	}

	private ClienteDTO defaultCliente() {
		logger.debug("Entra a defaultCliente. Se abrio el circuito");
		return new ClienteDTO();
	}

}
