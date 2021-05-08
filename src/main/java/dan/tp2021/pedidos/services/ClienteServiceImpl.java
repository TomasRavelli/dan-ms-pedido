package dan.tp2021.pedidos.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import dan.tp2021.pedidos.domain.Obra;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.domain.Producto;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.dto.ObraDTO;

@Service
public class ClienteServiceImpl implements ClienteService{

	@Override
	public ResponseEntity<ClienteDTO> getClienteByObra(Pedido p) {
		
		// Buscar en el servicio Usuario la obra, para encontrar a que cliente
		// pertenece.
		WebClient client = WebClient.create("http://localhost:8080/api/obra/" + p.getObra().getId());

		ResponseEntity<ObraDTO> response = client.get().accept(MediaType.APPLICATION_JSON).retrieve()
				.toEntity(ObraDTO.class).block();

		if (response.getStatusCode().equals(HttpStatus.OK)) {

			// TODO ver si se puede arreglar para que no entre dos veces a la API de
			// cliente. Esto sucede porque el JSON no tiene un cliente asociado.

			client = WebClient.create("http://localhost:8080/api/cliente/" + response.getBody().getIdCliente()); // Buscar
																													// los
																													// datos
																													// del
																													// cliente
																													// en
																													// el
																													// servicio
																													// de
																													// usuarios.

			ResponseEntity<ClienteDTO> clienteResponse = client.get().accept(MediaType.APPLICATION_JSON).retrieve()
					.toEntity(ClienteDTO.class).block();

			if (clienteResponse.getStatusCode().equals(HttpStatus.OK)) {
				return clienteResponse;
			}
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}



}
