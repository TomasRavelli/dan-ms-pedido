package dan.tp2021.pedidos.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import dan.tp2021.pedidos.dao.PedidoRepository;
import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.EstadoPedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.dto.ObraDTO;
import dan.tp2021.pedidos.exceptions.cliente.ClienteBadRequestException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteNoHabilitadoException;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;
import dan.tp2021.pedidos.exceptions.pedido.DetallePedidoNoEncontradoException;
import dan.tp2021.pedidos.exceptions.pedido.PedidoNoEncontradoException;
import io.micrometer.core.annotation.Timed;

@Service
public class PedidoServiceImpl implements PedidoService {

	private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);

	@Autowired
	PedidoRepository pedidoRepository;
	@Autowired
	ClienteService clienteServiceImpl;

	@Autowired
	BancoService bancoServiceImpl;

	@Autowired
	ObraService obraServiceImpl;

	@Autowired
	EstadoPedidoService estadoPedidoService;

	@Autowired
	MessageService messageService;

	/*
	 * @Override public Pedido savePedido(Pedido p) throws
	 * ClienteNoHabilitadoException, ClienteException, ObraNoEncontradaException,
	 * ClienteBadRequestException {
	 * 
	 * //TODO El pedido se guarda inicialmente con el estado NUEVO, hay que cambiar
	 * varias cosas en este método.
	 * 
	 * ClienteDTO clienteDTO = clienteServiceImpl.getClienteByObra(p); // Si
	 * llegamos acá quiere decir que no hubo error, porque cuando hay error se //
	 * lanza una excepción y la ejecución se corta.
	 * 
	 * logger.debug("Cliente buscado, ID: "+clienteDTO.getId()); double
	 * sumaCostosProductos = 0; boolean stockDisponible = true;
	 * 
	 * for (DetallePedido dp : p.getDetalle()) {
	 * 
	 * sumaCostosProductos += dp.getPrecio();
	 * 
	 * if (dp.getProducto().getStockActual() <= 0 && stockDisponible) {
	 * 
	 * stockDisponible = false;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(),
	 * superaDescubierto = sumaCostosProductos > clienteDTO.getMaxCuentaOnline();
	 * boolean condicionC = !superaDescubierto &&
	 * bancoServiceImpl.verificarSituacionCliente(clienteDTO); // TODO ver como
	 * tratamos el getHabilitadoOnline
	 * 
	 * if (!esDeudor || condicionC) {
	 * 
	 * if (stockDisponible) { p.setEstado(this.getEstadoPedido("ACEPTADO")); } else
	 * { p.setEstado(this.getEstadoPedido("PENDIENTE")); }
	 * 
	 * // p.setFechaPedido(Instant.now());
	 * 
	 * logger.debug("Material del primer detalle pedido?: " +
	 * p.getDetalle().get(0).getProducto());
	 * logger.debug("ID material del primer detalle pedido: " +
	 * p.getDetalle().get(0).getProducto().getId());
	 * 
	 * Pedido aux = pedidoRepository.save(p);
	 * messageService.sendMessageToProductos(aux);
	 * 
	 * return aux;
	 * 
	 * } else { //TODO preguntar si aca no hay que guardar igual el pedido con
	 * estado RECHAZADO. throw new
	 * ClienteNoHabilitadoException("Error. El cliente no cumple con las condiciones para adquirir el pedido"
	 * ); }
	 * 
	 * }
	 */

	public Pedido savePedido(Pedido p) throws ClienteNoHabilitadoException, ClienteException, ObraNoEncontradaException,
			ClienteBadRequestException {

		return pedidoRepository.save(p);

	}

	@Override
	public Pedido addItem(Integer idPedido, DetallePedido detalle) throws PedidoNoEncontradoException {

		Optional<Pedido> p = pedidoRepository.findById(idPedido);
		// TODO verificar que el pedido esté en estado NUEVO antes de agregar el
		// detalle.
		if (p.isPresent()) {
			Pedido ped = p.get();
			ped.getDetalle().add(detalle);
			return pedidoRepository.save(ped);
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	@Timed(description = "tiempo_actualizacion_pedido")
	public Pedido updatePedido(Integer idPedido, Pedido nuevoPedido) throws PedidoNoEncontradoException, ClienteException, ObraNoEncontradaException, ClienteBadRequestException, ClienteNoHabilitadoException {
		
		if (idPedido.equals(nuevoPedido.getId())) {
			if (pedidoRepository.existsById(idPedido)) {
				// OJO porque esto reescribe totalmente el pedido antiguo. Si hay atributos
				// nulos, los borrara.
				Pedido pedidoAnterior = this.getPedidoByID(idPedido);
				if (pedidoAnterior.getEstado().getEstado().equals(nuevoPedido.getEstado().getEstado())) {
					// verificar que no se esté intentando cambiar la obra, eso no está permitido.
					if (pedidoAnterior.getObra().getId() != nuevoPedido.getObra().getId()) {
						throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "No puede modificarse la obra.");
					}
					return pedidoRepository.save(nuevoPedido);
					} else {
					logger.debug("Entra al else: " + nuevoPedido.getEstado().getEstado() + ", "
							+ pedidoAnterior.getEstado().getEstado());
					boolean transicionProhibida = false;
					//TODO Comparar por Estado o por ID?
					switch (nuevoPedido.getEstado().getEstado()) {
					case "NUEVO": {
						transicionProhibida = true;
						break;
					}
					case "CONFIRMADO": {
						transicionProhibida = !pedidoAnterior.getEstado().getEstado().equals("NUEVO");
						if (!transicionProhibida) {
							return validarEstadoCliente(nuevoPedido);
						}
						break;
					}
					case "CANCELADO": {
						transicionProhibida = !pedidoAnterior.getEstado().getEstado().equals("NUEVO")
								&& !pedidoAnterior.getEstado().getEstado().equals("CONFIRMADO")
								&& !pedidoAnterior.getEstado().getEstado().equals("PENDIENTE");
						break;
					}
					case "PENDIENTE": {
						//Este estado no se puede elegir manualmente, cuando se pasa a CONFIRMADO, el servicio verifica automaticamente si pasa a este estado.
						transicionProhibida = true;
						break;
					}
					case "ACEPTADO": {
						transicionProhibida = !pedidoAnterior.getEstado().getEstado().equals("PENDIENTE");
						if (!transicionProhibida) {
							return validarEstadoCliente(nuevoPedido);
						}
						break;
					}
					case "RECHAZADO": {
						//Este estado no se puede elegir manualmente, cuando se pasa a CONFIRMADO, el servicio verifica automaticamente si pasa a este estado.
						transicionProhibida = true;
						break;
					}
					case "EN PREPARACION": {
						transicionProhibida = !pedidoAnterior.getEstado().getEstado().equals("ACEPTADO");
						break;
					}
					case "ENTREGADO": {
						transicionProhibida = !pedidoAnterior.getEstado().getEstado().equals("EN PREPARACION");
						break;
					}
					}
					if (transicionProhibida) {
						throw new HttpClientErrorException(HttpStatus.FORBIDDEN,
								"Transición de estado de pedido inválida.");
					}
					return pedidoRepository.save(nuevoPedido);
				}

			}
			throw new PedidoNoEncontradoException("Pedido inexistente.");
		}
		throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Los IDs no coinciden");
	}

	private Pedido validarEstadoCliente(Pedido p) throws ClienteException, ObraNoEncontradaException,
			ClienteBadRequestException, ClienteNoHabilitadoException {
		ClienteDTO clienteDTO = clienteServiceImpl.getClienteByObra(p); // Si llegamos acá quiere decir que no hubo
																		// error, porque cuando hay error se lanza una
																		// excepción y la ejecución se corta.

		logger.debug("Cliente buscado, ID: " + clienteDTO.getId());
		double sumaCostosProductos = 0;
		boolean stockDisponible = true;

		for (DetallePedido dp : p.getDetalle()) {

			sumaCostosProductos += dp.getPrecio();

			if (dp.getProducto().getStockActual() <= dp.getCantidad() && stockDisponible) {
				//TODO En vez de validar por stock actual no deberiamos validar por stock minimo
				stockDisponible = false;

			}

		}

		boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(),
				superaDescubierto = sumaCostosProductos > clienteDTO.getMaxCuentaOnline();
		boolean condicionC = !superaDescubierto && bancoServiceImpl.verificarSituacionCliente(clienteDTO); // TODO ver
																											// comotratamos
																											// el
																											// getHabilitadoOnline

		if (!esDeudor || condicionC) {

			if (stockDisponible) {
				p.setEstado(this.getEstadoPedido("ACEPTADO"));
			} else {
				p.setEstado(this.getEstadoPedido("PENDIENTE"));
			}

			// p.setFechaPedido(Instant.now());

			logger.debug("Material del primer detalle pedido?: " + p.getDetalle().get(0).getProducto());
			logger.debug("ID material del primer detalle pedido: " + p.getDetalle().get(0).getProducto().getId());

			Pedido aux = pedidoRepository.save(p);
			if(p.getEstado().getEstado().equals("ACEPTADO")){
				//Si el estado queda en PENDIENTE no debemos actualizar el stock de materiales.
				messageService.sendMessageToProductos(aux);
			}

			return aux;

		} else { // TODO preguntar si aca no hay que guardar igual el pedido conestado RECHAZADO.
			throw new ClienteNoHabilitadoException(
					"Error. El cliente no cumple con las condiciones para adquirir el pedido");
		}

	}

	@Override
	public Pedido deletePedidoById(Integer idPedido) throws PedidoNoEncontradoException {
		// TODO el pedido se elimina o se cambia a estado CANCELADO?
		Optional<Pedido> ped = pedidoRepository.findById(idPedido);
		if (ped.isPresent()) {

			pedidoRepository.delete(ped.get());

			return ped.get();
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public Pedido deleteDetallePedidoPedidoById(Integer idPedido, Integer idDetalle)
			throws DetallePedidoNoEncontradoException, PedidoNoEncontradoException {
		// TODO cuándo se podrían eliminar detalles? Solo cuando el pedido está en
		// estado NUEVO?
		Optional<Pedido> ped = pedidoRepository.findById(idPedido);
		if (ped.isPresent()) {
			Pedido p = ped.get();
			Optional<DetallePedido> det = p.getDetalle().stream().filter(dp -> dp.getId().equals(idDetalle))
					.findFirst();
			if (det.isPresent()) {
				p.getDetalle().remove(det.get());
				return pedidoRepository.save(p);
			}
			throw new DetallePedidoNoEncontradoException("Detalle de pedido inexistente.");
		}
		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public Pedido getPedidoByID(Integer idPedido) throws PedidoNoEncontradoException {

		Optional<Pedido> p = pedidoRepository.findById(idPedido);
		if (p.isPresent()) {
			return p.get();
		}
		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public List<Pedido> getPedidoByIdObra(Integer idObra) throws PedidoNoEncontradoException {
		List<Pedido> listaPedidos = pedidoRepository.findAll();
		List<Pedido> pedidos = listaPedidos.stream().filter(p -> p.getObra().getId().equals(idObra))
				.collect(Collectors.toList());
		if (!pedidos.isEmpty()) {
			return pedidos;
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public List<Pedido> getPedidosByClientParams(Integer idCliente, String cuitCliente, String estadoPedido)
			throws ObraNoEncontradaException {

		List<Pedido> listaPedidos;
		if (!estadoPedido.isBlank()) {
			listaPedidos = pedidoRepository.findByEstadoEstado(estadoPedido);
		} else {
			listaPedidos = pedidoRepository.findAll();
		}

		String queryString = "";

		if (idCliente.equals(0) && cuitCliente.isBlank()) {
			// retornar todos los pedidos
			return listaPedidos;
		}

		if (idCliente > 0) {
			queryString += "?idCliente=" + idCliente;
		}

		if (!cuitCliente.isEmpty()) {
			if (queryString.isEmpty()) {
				queryString += "?";
			} else {
				queryString += "&";
			}
			queryString += "cuitCliente=" + cuitCliente;
		}

		List<ObraDTO> obrasDTO = obraServiceImpl.getObrasByClienteParams(queryString); // Buscar obras al microservicio
																						// usuarios.
		if (obrasDTO == null) {
			throw new ObraNoEncontradaException("No se encontraron obras");
		}
		List<Pedido> pedidosFiltrados = filtrarPedidos(listaPedidos, obrasDTO); // Filtrar por pedidos que tengan las
																				// obras traidas.

		return pedidosFiltrados;
	}

	private List<Pedido> filtrarPedidos(List<Pedido> listaPedidos, List<ObraDTO> obrasDTO) {
		List<Pedido> pedidosFiltrados = listaPedidos.stream().filter(pedido -> {
			for (ObraDTO obra : obrasDTO) {
				if (pedido.getObra().getId().equals(obra.getId()))
					return true;
			}
			return false;
		}).collect(Collectors.toList());
		return pedidosFiltrados;
	}

	@Override
	public DetallePedido getDetallePedidoById(Integer idPedido, Integer id)
			throws DetallePedidoNoEncontradoException, PedidoNoEncontradoException {

		Optional<Pedido> ped = pedidoRepository.findById(idPedido);
		if (ped.isPresent()) {
			Pedido p = ped.get();
			Optional<DetallePedido> det = p.getDetalle().stream().filter(dp -> dp.getId().equals(id)).findFirst();
			if (det.isPresent()) {

				return det.get();
			}
			throw new DetallePedidoNoEncontradoException("Detalle de pedido inexistente.");
		}
		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public EstadoPedido getEstadoPedido(String estado) {
		return estadoPedidoService.findByEstado(estado);
	}
}
