package dan.tp2021.pedidos.rest;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.client.HttpClientErrorException;

import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteNoHabilitadoException;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;
import dan.tp2021.pedidos.exceptions.pedido.DetallePedidoNoEncontradoException;
import dan.tp2021.pedidos.exceptions.pedido.PedidoNoEncontradoException;
import dan.tp2021.pedidos.services.PedidoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/pedido")
@Api(value = "PedidoRest", description = "Se encarga de gestionar los pedidos de la empresa")
public class PedidoRest {

	private static final Logger logger = LoggerFactory.getLogger(PedidoRest.class);
	@Autowired
	PedidoService pedidoServiceImpl;

	@ApiOperation(value = "Crea un nuevo Pedido")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pedido creado correctamente"),
			@ApiResponse(code = 401, message = "No autorizado"), @ApiResponse(code = 403, message = "Prohibido") })
	@PostMapping
	public ResponseEntity<Pedido> crearPedido(@RequestBody Pedido nuevoPedido) {
		logger.debug("Entra al post");
		
		nuevoPedido.setEstado(pedidoServiceImpl.getEstadoNuevoPedido("NUEVO"));//el estado del pedido siempre es NUEVO al crear, busco este estado en la BD.
		
		logger.debug("Estado del pedido: " + nuevoPedido.getEstado().getEstado());
		
		if (nuevoPedido != null && nuevoPedido.getObra() != null && nuevoPedido.getDetalle() != null
				&& nuevoPedido.getDetalle().size() > 0) {
			
			for (DetallePedido d : nuevoPedido.getDetalle()) {
				if (d.getCantidad() == null || d.getProducto() == null) {
					return ResponseEntity.badRequest().build();
				}
			}
			logger.debug("Cumple las precondiciones en POST");
			try {
				Pedido rep = pedidoServiceImpl.savePedido(nuevoPedido);
				// Si no hay excepción es que se guardó correctamente.
				logger.info("ID NUEVA ENTIDAD: "+ rep.getId());
				return ResponseEntity.ok(rep);

			} catch (ClienteNoHabilitadoException e) {
				// Error, el cliente no está habilitado Responde 400?
				logger.warn("Cliente no habilitado. Mensaje: "+ e.getMessage(), e);
				return ResponseEntity.badRequest().build();
			} catch (ClienteException e) {
				// Respondo Internal server error (500) porque me parece que esto no es un
				// problema de los datos que mandó el cliente.

				logger.error("Cliente exception. Mensaje: "+ e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			} catch (Exception e) {

				logger.error("Error desconocido. Mensaje: "+ e.getMessage(), e);
				// Esto definitivamente es un 500, error desconocido e inesperado.
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}

		logger.warn("No se cumplieron las precondiciones.");
		// No hay que devolver el pedido acá, porque no se guardó, no es válido.
		return ResponseEntity.badRequest().build();

	}

	@ApiOperation("Agregar un item a un pedido existente")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Item agregado exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "Pedido no encontrado"), })
	@PostMapping("/{idPedido}/detalle")
	public ResponseEntity<Pedido> agregarItemAPedido(@PathVariable Integer idPedido,
			@RequestBody DetallePedido detalle) {
		logger.debug("Entra a crear nuevo detalle de pedido");
		try {
			Pedido p = pedidoServiceImpl.addItem(idPedido, detalle);
			return ResponseEntity.ok(p);
		} catch (PedidoNoEncontradoException e) {
			logger.warn("Pedido no encontrado. Mensaje: "+e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			logger.error("Error desconocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@ApiOperation("Actualizar un pedido existente")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pedido actualizado exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "Pedido no encontrado"), })
	@PutMapping("/{idPedido}")
	public ResponseEntity<Pedido> actualizarPedido(@PathVariable Integer idPedido, @RequestBody Pedido nuevoPedido) {
		logger.debug("entra a actualizar pedido. Id: " + idPedido);
		try {
			Pedido p = pedidoServiceImpl.updatePedido(idPedido, nuevoPedido);
			return ResponseEntity.ok(p);
		} catch (PedidoNoEncontradoException e) {
			logger.warn("PEdido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (HttpClientErrorException e) {
			logger.error("Http Client Error. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(e.getStatusCode()).build();
		} catch (Exception e) {

			logger.error("Error desconocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@ApiOperation("Borrar por su ID un pedido existente")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pedido borrado exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "Pedido no encontrado"), })
	@DeleteMapping("/{idPedido}")
	public ResponseEntity<Pedido> deletePedido(@PathVariable Integer idPedido) {
		logger.debug("Entra a borrar pedido. IdPedido: " + idPedido);
		try {
			Pedido p = pedidoServiceImpl.deletePedidoById(idPedido);
			return ResponseEntity.ok(p);
		} catch (PedidoNoEncontradoException e) {
			logger.warn("PEdido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			logger.error("Error no conocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@ApiOperation("Borrar un item de un pedido existente")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Item de pedido borrado exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "Item/Pedido no encontrado"), })

	@DeleteMapping("/{idPedido}/detalle/{idDetalle}")
	public ResponseEntity<Pedido> deleteItemPedido(@PathVariable Integer idPedido, @PathVariable Integer idDetalle) {

		logger.debug("Entra borrar item de pedido. IdPEdido: " + idPedido + ", IdItem: " + idDetalle);
		try {
			Pedido p = pedidoServiceImpl.deleteDetallePedidoPedidoById(idPedido, idDetalle);
			return ResponseEntity.ok(p);
		} catch (PedidoNoEncontradoException e) {

			logger.warn("Pedido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (DetallePedidoNoEncontradoException e) {

			logger.warn("Detalle no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {

			logger.error("Error desconocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@ApiOperation("Obtener un pedido por su ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pedido encontrado exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "Pedido no encontrado"), })

	@GetMapping("/{idPedido}")
	public ResponseEntity<Pedido> getPedidoById(@PathVariable Integer idPedido) {
		logger.debug("Entra al get pedido by id. IdPedido: " + idPedido);
		try {
			Pedido p = pedidoServiceImpl.getPedidoByID(idPedido);
			return ResponseEntity.ok(p);
		} catch (PedidoNoEncontradoException e) {
			logger.warn("Pedido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {

			logger.error("Error desconocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@ApiOperation(value = "Buscar pedidos por id de obra")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pedido encontrado exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "Pedidos no encontrados"), })

	@GetMapping("/porObra/{idObra}")
	public ResponseEntity<List<Pedido>> getPedidoByIdObra(@PathVariable Integer idObra) {
		logger.debug("Entra a getPedidoByIdObra. IdObra: " + idObra);
		try {
			List<Pedido> p = pedidoServiceImpl.getPedidoByIdObra(idObra);
			return ResponseEntity.ok(p);
		} catch (PedidoNoEncontradoException e) {
			//TODO no se encontró un pedido con essta obra, no creo que sea un error. Pueden existir obras sin pedidos.
			logger.warn("Pedido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {

			logger.error("Error desconocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@ApiOperation(value = "Buscar pedidos por id de cliente y/o cuit de cliente")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pedidos encontrados exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "Pedidos no encontrados"), })

	@GetMapping()
	public ResponseEntity<List<Pedido>> getPedidoByParams(
			@RequestParam(required = false, defaultValue = "0") Integer idCliente,
			@RequestParam(required = false, defaultValue = "") String cuitCliente,
			@RequestParam(required = false, defaultValue = "") String estadoPedido) {
		
		logger.debug("Entra al get de todos los los pedidos");
		try {
			List<Pedido> p = pedidoServiceImpl.getPedidosByClientParams(idCliente, cuitCliente, estadoPedido);
			return ResponseEntity.ok(p);
		} catch (PedidoNoEncontradoException e) {
			//TODO Puede haber clientes sin pedidos, eso no es un error.
			logger.warn("Pedido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (ObraNoEncontradaException e) {
			//TODO VER, clientes sin obras es posible? No debería, pero igual 404 me parece raro
			logger.warn("Obra no encontrada. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {

			logger.error("Error desconocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@ApiOperation(value = "Buscar detalle de pedido por ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Detalle de pedido encontrado exitosamente"),
			@ApiResponse(code = 401, message = "Falta autorizacion"), @ApiResponse(code = 403, message = "PROHIBIDO"),
			@ApiResponse(code = 404, message = "No existe detalle de pedido o pedido"), })

	@GetMapping("/{idPedido}/detalle/{id}")
	public ResponseEntity<DetallePedido> getDetallePedidoById(@PathVariable Integer idPedido,
			@PathVariable Integer id) {
		DetallePedido detalleResultado;
		logger.debug("Get detalle pedido by ID");
		try {
			
			detalleResultado = pedidoServiceImpl.getDetallePedidoById(idPedido, id);
			
			return ResponseEntity.ok(detalleResultado);

		} catch (PedidoNoEncontradoException e) {

			logger.warn("Pedido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();

		} catch (DetallePedidoNoEncontradoException e) {

			logger.warn("Detalle Pedido no encontrado. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.notFound().build();
		
		} catch (Exception e) {

			logger.error("Error desconocido. Mensaje: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
