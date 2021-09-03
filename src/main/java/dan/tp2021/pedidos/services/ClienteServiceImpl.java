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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import dan.tp2021.pedidos.config.UsuarioRestProperties;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.dto.ObraDTO;
import dan.tp2021.pedidos.exceptions.cliente.ClienteBadRequestException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;

@Service
public class ClienteServiceImpl implements ClienteService {

	private static final Logger logger = LoggerFactory.getLogger(ClienteServiceImpl.class);

	@Autowired
	UsuarioRestProperties usuarioRestProperties;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	@Override
	public ClienteDTO getClienteByObra(Pedido p) throws ClienteException, ObraNoEncontradaException, ClienteBadRequestException {

		// Buscar en el servicio Usuario la obra, para encontrar a que cliente
		// pertenece.

		String url = usuarioRestProperties.getUrl();
		logger.debug("Url de usuarios: " + url);

		WebClient client = WebClient.create(url + "/api");
		
		//Si el circuit breaker se abre, decidimos que se lance igualmente una excepcion 
		//porque no se puede crear el pedido sin tener los datos validos.
		CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

		ResponseEntity<ObraDTO> response = circuitBreaker.run(() -> {
			return client.get().uri("/obra/" + p.getObra().getId())
					.accept(MediaType.APPLICATION_JSON).retrieve().toEntity(ObraDTO.class).block();
		}, throwable -> defaultObra(throwable));
		
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			// TODO ver si se puede arreglar para que no entre dos veces a la API de
			// cliente.
			// Esto sucede porque el JSON no tiene un cliente asociado.
			// Podemos hacer que se pueda buscar clietnes por id de obra...

			// Buscar los datos del cliente en el servicio de usuarios.

			logger.debug("ID obra buscada: " + response.getBody().getId());
			ObraDTO obra = response.getBody();

			ResponseEntity<ClienteDTO> clienteResponse = circuitBreaker.run(() -> {
				return client.get().uri("/cliente/" + obra.getCliente().getId())
						.accept(MediaType.APPLICATION_JSON).retrieve().toEntity(ClienteDTO.class).block();
			}, throwable -> defaultCliente(throwable));
			
			if (clienteResponse.getStatusCode().equals(HttpStatus.OK)) {
				return clienteResponse.getBody();
			}
			else {
				if (clienteResponse.getStatusCode().is4xxClientError()) {
					throw new ClienteBadRequestException("Error en la comunicacion con el servicio de usuarios.");
				}
			}
		}
		else {

			if(response.getStatusCode().is4xxClientError()) {
				if(response.getStatusCode() == HttpStatus.NOT_FOUND) {
					throw new ObraNoEncontradaException("Error. Obra no encontrada");
				}
				throw new ClienteBadRequestException("Error en la comunicacion con el servicio de usuarios.");
			}
		}
		
		throw new ClienteException("Error en la comunicacion con el servicio de usuarios.");

	}

	private ResponseEntity<ClienteDTO> defaultCliente(Throwable throwable) {
		logger.debug("Entra a defaultCliente. Se abrio el circuito");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
	
	private ResponseEntity<ObraDTO> defaultObra(Throwable t) {
		logger.debug("Entra a defaultObra. Se abrio el circuito");
		logger.error(t.getClass().getName());
		if(t instanceof WebClientResponseException.NotFound) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		else {
			if(t instanceof WebClientResponseException.BadRequest) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

}
