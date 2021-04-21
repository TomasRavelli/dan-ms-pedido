package dan.tp2021.pedidos.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ObraDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/pedido")
@Api(value="PedidoRest", description = "Se encarga de gestionar los pedidos de la empresa")
public class PedidoRest {
	
	private static Integer ID_GEN=1;
	private static List<Pedido> listaPedidos = new ArrayList<>();
	
	
	@ApiOperation(value = "Crea un nuevo Pedido")
	    @ApiResponses(value = {
	            @ApiResponse(code = 200, message = "Pedido creado correctamente"),
	            @ApiResponse(code = 401, message = "No autorizado"),
	            @ApiResponse(code = 403, message = "Prohibido")
	    })
	@PostMapping
	public ResponseEntity<Pedido> crearPedido(@RequestBody Pedido nuevoPedido){

    	System.out.println(" crear pedido "+nuevoPedido);
        nuevoPedido.setId(ID_GEN++);
        listaPedidos.add(nuevoPedido);
        return ResponseEntity.ok(nuevoPedido);
    
	}
	
	@ApiOperation("Agregar un item a un pedido existente")
	@ApiResponses(value = {
			@ApiResponse(code=200, message="Item agregado exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="Pedido no encontrado"),
	})
	@PostMapping("/{idPedido}/detalle")
	public ResponseEntity<Pedido> agregarItemAPedido(@PathVariable Integer idPedido, @RequestBody DetallePedido detalle){
		
		OptionalInt indexOpt =  IntStream.range(0, listaPedidos.size())
                .filter(i -> listaPedidos.get(i).getId().equals(idPedido))
                .findFirst(); 
		
		if(indexOpt.isPresent()) {
			Pedido p = listaPedidos.get(indexOpt.getAsInt());
			p.getDetalle().add(detalle);
			listaPedidos.set(indexOpt.getAsInt(), p);
			return ResponseEntity.ok(p);
		}	
		else {
			return ResponseEntity.notFound().build();
		}
	
	}
	
	@ApiOperation("Actualizar un pedido existente")
	@ApiResponses(value = {
			@ApiResponse(code=200, message="Pedido actualizado exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="Pedido no encontrado"),
	})
	@PutMapping("/{idPedido}")
	public ResponseEntity<Pedido> actualizarPedido(@PathVariable Integer idPedido, @RequestBody Pedido nuevoPedido){
		
		OptionalInt indexOpt =  IntStream.range(0, listaPedidos.size())
                .filter(i -> listaPedidos.get(i).getId().equals(idPedido))
                .findFirst(); 
		
		if(indexOpt.isPresent()) {
			listaPedidos.set(indexOpt.getAsInt(), nuevoPedido);
			return ResponseEntity.ok(nuevoPedido);
		}	
		else {
			return ResponseEntity.notFound().build();
		}
	
	}
	
	@ApiOperation("Borrar por su ID un pedido existente")
	@ApiResponses(value = {
			@ApiResponse(code=200, message="Pedido borrado exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="Pedido no encontrado"),
	})
	@DeleteMapping("/{idPedido}")
	public ResponseEntity<Pedido> deletePedido(@PathVariable Integer idPedido){
		
		OptionalInt indexOpt =  IntStream.range(0, listaPedidos.size())
                .filter(i -> listaPedidos.get(i).getId().equals(idPedido))
                .findFirst(); 
		
		if(indexOpt.isPresent()) {
			return ResponseEntity.ok(listaPedidos.remove(indexOpt.getAsInt()));
		}	
		else {
			return ResponseEntity.notFound().build();
		}
	
	}
	
	@ApiOperation("Borrar un item de un pedido existente")
	@ApiResponses(value = {
			@ApiResponse(code=200, message="Item de pedido borrado exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="Item/Pedido no encontrado"),
	})
	
	@DeleteMapping("/{idPedido}/detalle/{idDetalle}")
	public ResponseEntity<Pedido> deleteItemPedido(@PathVariable Integer idPedido, @PathVariable Integer idDetalle){
		
		OptionalInt indexOpt =  IntStream.range(0, listaPedidos.size())
                .filter(i -> listaPedidos.get(i).getId().equals(idPedido))
                .findFirst(); 
		
		if(indexOpt.isPresent()) {
			
			OptionalInt indexDetOpt =  IntStream.range(0, listaPedidos.get(indexOpt.getAsInt()).getDetalle().size())
	                .filter(i -> listaPedidos.get(indexOpt.getAsInt()).getDetalle().get(i).getId().equals(idDetalle))
	                .findFirst(); 
			if(indexDetOpt.isPresent()) {
				listaPedidos.get(indexOpt.getAsInt()).getDetalle().remove(indexDetOpt.getAsInt());
				
				return ResponseEntity.ok(listaPedidos.get(indexOpt.getAsInt()));
			}
			
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.notFound().build();
		}
	
	}
	
	
	@ApiOperation("Obtener un pedido por su ID")
	@ApiResponses(value = {
			@ApiResponse(code=200, message="Pedido encontrado exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="Pedido no encontrado"),
	})
	
	@GetMapping("/{idPedido}")
	public ResponseEntity<Pedido> getPedidoById(@PathVariable Integer idPedido){
		
		Optional<Pedido> pedido = listaPedidos
				.stream()
				.filter(p -> p.getId().equals(idPedido))
				.findFirst();
		
		if(pedido.isPresent()) {
			return ResponseEntity.ok(pedido.get());
		}
		else {
			return ResponseEntity.notFound().build();
		}
	
	}
	
	@ApiOperation(value = "Buscar pedidos por id de obra")
	@ApiResponses(value= {
			@ApiResponse(code=200, message = "Pedido encontrado exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="Pedidos no encontrados"),
	})
	
	@GetMapping("/porObra/{idObra}")
	public ResponseEntity<List<Pedido>> getPedidoByIdObra(@PathVariable Integer idObra){
		
		List<Pedido> listaRes = new ArrayList<>();
		for(Pedido p: listaPedidos) {
			if(p.getObra().getId().equals(idObra)) {
				listaRes.add(p);
			}
		}
		if(!listaRes.isEmpty()) {
			return ResponseEntity.ok(listaRes);
		}
		else {
			return ResponseEntity.notFound().build();
		}
	
	}
	
	@ApiOperation(value = "Buscar pedidos por id de cliente y/o cuit de cliente")
	@ApiResponses(value= {
			@ApiResponse(code=200, message = "Pedidos encontrados exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="Pedidos no encontrados"),
	})
	
	@GetMapping()
	public ResponseEntity<List<Pedido>> getPedidoByIdClienteOrCuitCliente(@RequestParam(required = false, defaultValue = "0") Integer idCliente, @RequestParam(required = false, defaultValue = "") String cuitCliente){

		if(idCliente == 0 && cuitCliente.isEmpty()){
			//No recibí ningún parámetro, retorno todos los pedidos.
			return ResponseEntity.ok(listaPedidos);
		}

		List<Pedido> pedidos = new ArrayList<>();
		WebClient client = WebClient.create("http://localhost:8080/api/obra");

		String queryString = "";

		if(idCliente > 0){
			queryString += "?idCliente="+idCliente;
		}

		if(!cuitCliente.isEmpty()){
			if(queryString.isEmpty()){
				queryString += "?";
			}else{
				queryString = "&";
			}
			queryString += "cuitCliente="+cuitCliente;
		}

		ResponseEntity<List<ObraDTO>> response = client.get()
												 .uri(queryString)
												 .accept(MediaType.APPLICATION_JSON)
												 .retrieve()
												 .toEntityList(ObraDTO.class)
												 .block();
		if(response != null && response.getStatusCode() == HttpStatus.OK){
			System.out.println("Respuesta exitosa de la api de obras: \n" + response);
			List<ObraDTO> obraDTOList = response.getBody();
			List<Pedido> pedidosFiltrados = listaPedidos.stream()
					.filter(pedido -> {
						for(ObraDTO obra: obraDTOList) {
							if (pedido.getObra().getId().equals(obra.getId())) return true;
						}
						return false;
					})
					.collect(Collectors.toList());

			return ResponseEntity.ok(pedidosFiltrados);
		}

		System.out.println("Hubo un error en el llamado a la api de obras.\n" + response);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

//		Flux<ObraDTO> result = client.get()
//				.uri(queryString)
//				.accept(MediaType.APPLICATION_JSON)
//				.retrieve()
//				.bodyToFlux(ObraDTO.class);
//		result.subscribe(obra -> filtrarObra(obra,pedidos));

//		if(idCliente.equals(null)&&cuitCliente.equals(null)) {
//			return ResponseEntity.badRequest().build();
//		}

//			if(!idCliente.equals(null)) {
//
//				Flux<ObraDTO> result = client.get()
//				.uri("/buscarPorParametros?idCliente="+idCliente.toString())
//				.accept(MediaType.APPLICATION_JSON)
//				.retrieve()
//				.bodyToFlux(ObraDTO.class);
//				result.subscribe(obra -> filtrarObra(obra,pedidos));
//			}
//
//			if(!cuitCliente.equals(null)) {
//
//				Flux<ObraDTO> result = client.get()
//			    .uri("/buscarPorCuit?cuitCliente="+cuitCliente.toString())
//				.accept(MediaType.APPLICATION_JSON)
//				.retrieve()
//				.bodyToFlux(ObraDTO.class);
//				result.subscribe(obra -> filtrarObra(obra,pedidos));
//			}
//
		
//		System.out.println(pedidos.size());
//		return ResponseEntity.ok(pedidos);
		
	}
	
	private void filtrarObra(ObraDTO obra, List<Pedido> p){
		for(Pedido ped: listaPedidos) {
			if(ped.getObra().equals(obra.getId()) && !p.contains(ped)) {
				p.add(ped);
			}
		}
	} 
	
	@ApiOperation(value = "Buscar detalle de pedido por ID")
	@ApiResponses(value= {
			@ApiResponse(code=200, message = "Detalle de pedido encontrado exitosamente"),
			@ApiResponse(code=401, message="Falta autorizacion"),
			@ApiResponse(code=403, message="PROHIBIDO"),
			@ApiResponse(code=404, message="No existe detalle de pedido o pedido"),
	})
	
	@GetMapping("/api/pedido/{idPedido}/detalle/{id}")
	public ResponseEntity<DetallePedido> getDetallePedidoById(@PathVariable Integer idPedido, @PathVariable Integer id){
		DetallePedido detalleResultado;
		
		Optional<Pedido> pedido = listaPedidos
				.stream()
				.filter(p -> p.getId().equals(idPedido))
				.findFirst();
		
		if(pedido.isPresent()) {
			Optional<DetallePedido> detallePed = pedido.get().getDetalle()
					.stream()
					.filter(dp -> dp.getId().equals(id))
					.findFirst();
			if(detallePed.isPresent()) {
				return ResponseEntity.ok(detallePed.get());
			}
			else {
				//Detalle no encontrado
				return ResponseEntity.notFound().build();
			}
		}
		
		else {
			//PEDIDO NO ENCONTRADO
			return ResponseEntity.notFound().build();
		}
			
	}

}

