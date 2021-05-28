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
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteNoHabilitadoException;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;
import dan.tp2021.pedidos.exceptions.pedido.DetallePedidoNoEncontradoException;
import dan.tp2021.pedidos.exceptions.pedido.PedidoNoEncontradoException;

@Service
public class PedidoServiceImpl implements PedidoService {

	private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);

	@Autowired
//	PedidoRepositoryInMemory pedidoRepositoryInMemory;
//	PedidoH2Repository pedidoRepositoryInMemory;
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
	JmsTemplate jms;

	@Override
	public Pedido savePedido(Pedido p) throws ClienteNoHabilitadoException, ClienteException {

		ClienteDTO clienteDTO = clienteServiceImpl.getClienteByObra(p); //TODO aqui podriamos llamar a ObraService para que guarde la obra en la BD y asi establecer la relacion.
		// Si llegamos acá quiere decir que no hubo error, porque cuando hay error se
		// lanza una excepción y la ejecución se corta.

		logger.debug("Cliente buscado, ID: "+clienteDTO.getId());
		double sumaCostosProductos = 0;
		boolean stockDisponible = true;

		for (DetallePedido dp : p.getDetalle()) {

			sumaCostosProductos += dp.getPrecio();

			if (dp.getProducto().getStockActual() <= 0 && stockDisponible) {

				stockDisponible = false;

			}

		}

		boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(),
				superaDescubierto = sumaCostosProductos > clienteDTO.getMaxCuentaOnline();
		boolean condicionC = !superaDescubierto && bancoServiceImpl.verificarSituacionCliente(clienteDTO); // TODO ver como tratamos el getHabilitadoOnline

		if (!esDeudor || condicionC) {

			if (stockDisponible) {
				p.setEstado(this.getEstadoPedido("ACEPTADO"));
			} else {
				p.setEstado(this.getEstadoPedido("PENDIENTE"));
			}

			p.setFechaPedido(Instant.now());//Esta bien? Segun la guia 6 va la fecha de envio solicitada por el cliente.
			// TODO ver que hacer si otras implementaciones de este método lanzan excepciones.
			// Las lanzamos hacia arriba y que se encarque el controller? O la capturamos y lanzamos otra excepción más "linda"?

			logger.debug("Material del primer detalle pedido?: " + p.getDetalle().get(0).getProducto());
			logger.debug("ID material del primer detalle pedido: " + p.getDetalle().get(0).getProducto().getId());
			// TODO ver que hacer si otras implementaciones de este método lanzan
			// excepciones.
			// Las lanzamos hacia arriba y que se encarque el controller? O la capturamos y
			// lanzamos otra excepción más "linda"?

			HashMap<String, Integer> detalles = new HashMap<>();
			for(DetallePedido d: p.getDetalle()){
				detalles.put(d.getProducto().getId().toString(),d.getCantidad());
				//Al id lo mando como String porque el mensaje necesita un hashmap con la key en String
			}
			jms.convertAndSend("COLA_PEDIDOS",detalles);
			return pedidoRepository.save(p);

		} else {
			//TODO preguntar si aca no hay que guardar igual el pedido con estado RECHAZADO.
			throw new ClienteNoHabilitadoException(
					"Error. El cliente no cumple con las condiciones para adquirir el pedido");
		}

	}

	@Override
	public Pedido addItem(Integer idPedido, DetallePedido detalle) throws PedidoNoEncontradoException {

		Optional<Pedido> p = pedidoRepository.findById(idPedido);

		if (p.isPresent()) {
			Pedido ped = p.get();
			ped.getDetalle().add(detalle);
			return pedidoRepository.save(ped);
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public Pedido updatePedido(Integer idPedido, Pedido nuevoPedido) throws PedidoNoEncontradoException {

		if (idPedido.equals(nuevoPedido.getId())) {
			if (pedidoRepository.existsById(idPedido)) {
				// OJO porque esto reescribe totalmente el pedido antiguo. Si hay atributos
				// nulos, los borrara.
				return pedidoRepository.save(nuevoPedido);
			}
			throw new PedidoNoEncontradoException("Pedido inexistente.");
		}
		throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Los IDs no coinciden");
	}

	@Override
	public Pedido deletePedidoById(Integer idPedido) throws PedidoNoEncontradoException {

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
		Optional<Pedido> ped = pedidoRepository.findById(idPedido);
		if (ped.isPresent()) {
			Pedido p = ped.get();
			Optional<DetallePedido> det = p.getDetalle().stream().filter(dp -> dp.getId().equals(idDetalle))
					.findFirst();
			if (det.isPresent()) {
				p.getDetalle().remove(det.get());
				// TODO habria que eliminar de la "tabla" DetallePedido en una futura BD?
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
		List<Pedido> listaPedidos = new ArrayList<>();
		pedidoRepository.findAll().forEach(p -> listaPedidos.add(p));
		List<Pedido> pedidos = listaPedidos.stream().filter(p -> p.getObra().getId().equals(idObra))
				.collect(Collectors.toList());
		if (!pedidos.isEmpty()) {
			return pedidos;
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public List<Pedido> getPedidosByClientParams(Integer idCliente, String cuitCliente, String estadoPedido)
			throws PedidoNoEncontradoException, ObraNoEncontradaException {
		
		List<Pedido> listaPedidos = new ArrayList<>();
		if(!estadoPedido.isBlank()) {
			listaPedidos = pedidoRepository.findByEstadoEstado(estadoPedido);
		}
		else {
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
		
		List<ObraDTO> obrasDTO = obraServiceImpl.getObrasByClienteParams(queryString); //Buscar obras al microservicio usuarios.
		List<Pedido> pedidosFiltrados = filtrarPedidos(listaPedidos, obrasDTO); //Filtrar por pedidos que tengan las obras traidas.

		return pedidosFiltrados;
		/*
			if (!pedidosFiltrados.isEmpty()) {
				return pedidosFiltrados;
			}
			throw new PedidoNoEncontradoException("No se encontraron pedidos que cumplan con estos criterios.");*/
		

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
			Optional<DetallePedido> det = p.getDetalle().stream().filter(dp -> dp.getId().equals(id))
					.findFirst();
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
