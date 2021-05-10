package dan.tp2021.pedidos.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import dan.tp2021.pedidos.dto.ObraDTO;

public class ObraServiceImpl implements ObraService {

	@Override
	public List<ObraDTO> getObrasByClienteParams(String s) throws ObraNoEncontradaException, HttpServerErrorException {

		WebClient client = WebClient.create("http://localhost:8080/api/obra");

		ResponseEntity<List<ObraDTO>> response = client
													.get()
													.uri(s)
													.accept(MediaType.APPLICATION_JSON)
													.retrieve()
													.toEntityList(ObraDTO.class)
													.block();

		if (response != null && response.getStatusCode() == HttpStatus.OK) {
			System.out.println("Respuesta exitosa de la api de obras: \n" + response);
			if (!response.getBody().isEmpty()) {
				return response.getBody();
			}

			throw new ObraNoEncontradaException("No se encontraron obras");
		}

		throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
				"Error al buscar datos en microservicio usuarios");
	}

}
