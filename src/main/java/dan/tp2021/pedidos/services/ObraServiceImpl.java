package dan.tp2021.pedidos.services;

import java.util.ArrayList;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import dan.tp2021.pedidos.config.UsuarioRestProperties;
import dan.tp2021.pedidos.dto.ObraDTO;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;

@Service
public class ObraServiceImpl implements ObraService {
	private static final Logger logger = LoggerFactory.getLogger(ObraServiceImpl.class);

	@Autowired
	UsuarioRestProperties usuarioRestProperties;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	@Override
	public List<ObraDTO> getObrasByClienteParams(String s) throws ObraNoEncontradaException, HttpServerErrorException {

		logger.debug("Entra a buscar la obra");

		String url = usuarioRestProperties.getUrl();
		logger.debug("Url de pedidos: " + url);

		WebClient client = WebClient.create(url + "/api/obra");

		CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

		return circuitBreaker.run(() -> {
			ResponseEntity<List<ObraDTO>> response = client.get().uri(s).accept(MediaType.APPLICATION_JSON).retrieve()
					.toEntityList(ObraDTO.class).block();

			if (response != null && response.getStatusCode() == HttpStatus.OK) {
				logger.debug("Obras buscadas. Size: " + response.getBody().size());
				if (!response.getBody().isEmpty()) {
					return response.getBody();
				}

//				throw new ObraNoEncontradaException("No se encontraron obras");
				return null;
			}

			throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error al buscar datos en microservicio usuarios");
		}, throwable -> defaultObrasDTO());

	}

	private List<ObraDTO> defaultObrasDTO() {
		logger.debug("Entra a defaultObrasDTO. Se abrio el circuito");
		return new ArrayList<ObraDTO>();
	}

}
