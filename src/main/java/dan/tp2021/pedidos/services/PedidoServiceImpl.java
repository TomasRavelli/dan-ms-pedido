package dan.tp2021.pedidos.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.client.HttpServerErrorException;

import dan.tp2021.pedidos.dao.PedidoRepositoryInMemory;
import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.EstadoPedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.dto.ObraDTO;
import dan.tp2021.pedidos.services.ObraService.ObraNoEncontradaException;

@Service
public class PedidoServiceImpl implements PedidoService {

	@Autowired
	PedidoRepositoryInMemory pedidoRepositoryInMemory;

	@Autowired
	ClienteService clienteServiceImpl;

	@Autowired
	BancoService bancoServiceImpl;

	@Autowired
	ObraService obraServiceImpl;

	@Override
	public Pedido savePedido(Pedido p) throws ClienteNoHabilitadoException, ClienteService.ClienteException {

		EstadoPedido estadoPedido = new EstadoPedido();

		ClienteDTO clienteDTO = clienteServiceImpl.getClienteByObra(p);
		// Si llegamos acá quiere decir que no hubo error, porque cuando hay error se
		// lanza una excepción y la ejecución se corta.

		double sumaCostosProductos = 0;
		boolean stockDisponible = true;

		for (DetallePedido dp : p.getDetalle()) {

			sumaCostosProductos += dp.getPrecio();
			// Aca se deberia usar el materialService creo. COMENTARIO DE PERU.
			// Eso estaba implementado por el profe, si quieren lo hacemos como el.
			// COMENTARIO TOMAS.
//				if(materialService.stockDisponible(dp.getProducto()) <= 0 && stockDisponible){
//					stockDisponible = false;
//				}
			if (dp.getProducto().getStockActual() <= 0 && stockDisponible) {

				stockDisponible = false;

			}

		}

		boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(),
				superaDescubierto = sumaCostosProductos > clienteDTO.getMaxCuentaOnline();
		boolean condicionC = !superaDescubierto && bancoServiceImpl.verificarSituacionCliente(clienteDTO); // TODO ver
																											// como
																											// tratamos
																											// el
																											// getHabilitadoOnline

		if (!esDeudor || condicionC) {

			if (stockDisponible) {
				estadoPedido.setEstado("ACEPTADO");
			} else {
				estadoPedido.setEstado("PENDIENTE");
			}

			p.setEstado(estadoPedido);
			// TODO ver que hacer si otras implementaciones de este método lanzan
			// excepciones.
			// Las lanzamos hacia arriba y que se encarque el controller? O la capturamos y
			// lanzamos otra excepción más "linda"?
			return pedidoRepositoryInMemory.save(p);

		} else {
			throw new ClienteNoHabilitadoException(
					"Error. El cliente no cumple con las condiciones para adquirir el pedido");
		}

	}

	@Override
	public Pedido addItem(Integer idPedido, DetallePedido detalle) throws PedidoNoEncontradoException {

		Optional<Pedido> p = pedidoRepositoryInMemory.findById(idPedido);

		if (p.isPresent()) {
			Pedido ped = p.get();
			ped.getDetalle().add(detalle);
			return pedidoRepositoryInMemory.save(ped);
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public Pedido updatePedido(Integer idPedido, Pedido nuevoPedido) throws PedidoNoEncontradoException {

		if (idPedido.equals(nuevoPedido.getId())) {
			if (pedidoRepositoryInMemory.existsById(idPedido)) {
				// OJO porque esto reescribe totalmente el pedido antiguo. Si hay atributos
				// nulos, los borrara.
				return pedidoRepositoryInMemory.save(nuevoPedido);
			}
			throw new PedidoNoEncontradoException("Pedido inexistente.");
		}
		throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Los IDs no coinciden");
	}

	@Override
	public Pedido deletePedidoById(Integer idPedido) throws PedidoNoEncontradoException {

		Optional<Pedido> ped = pedidoRepositoryInMemory.findById(idPedido);
		if (ped.isPresent()) {

			pedidoRepositoryInMemory.delete(ped.get());

			return ped.get();
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public Pedido deleteDetallePedidoPedidoById(Integer idPedido, Integer idDetalle)
			throws DetallePedidoNoEncontradoException, PedidoNoEncontradoException {
		Optional<Pedido> ped = pedidoRepositoryInMemory.findById(idPedido);
		if (ped.isPresent()) {
			Pedido p = ped.get();
			Optional<DetallePedido> det = p.getDetalle().stream().filter(dp -> dp.getId().equals(idDetalle))
					.findFirst();
			if (det.isPresent()) {
				p.getDetalle().remove(det.get());
				// TODO habria que eliminar de la "tabla" DetallePedido en una futura BD?
				return pedidoRepositoryInMemory.save(p);
			}
			throw new DetallePedidoNoEncontradoException("Detalle de pedido inexistente.");
		}
		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public Pedido getPedidoByID(Integer idPedido) throws PedidoNoEncontradoException {

		Optional<Pedido> p = pedidoRepositoryInMemory.findById(idPedido);
		if (p.isPresent()) {
			return p.get();
		}
		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public List<Pedido> getPedidoByIdObra(Integer idObra) throws PedidoNoEncontradoException {
		List<Pedido> listaPedidos = new ArrayList<>();
		pedidoRepositoryInMemory.findAll().forEach(p -> listaPedidos.add(p));
		List<Pedido> pedidos = listaPedidos.stream().filter(p -> p.getObra().getId().equals(idObra))
				.collect(Collectors.toList());
		if (!pedidos.isEmpty()) {
			return pedidos;
		}

		throw new PedidoNoEncontradoException("Pedido inexistente.");
	}

	@Override
	public List<Pedido> getPedidosByClientParams(Integer idCliente, String cuitCliente)
			throws PedidoNoEncontradoException, ObraNoEncontradaException {
		
		List<Pedido> listaPedidos = new ArrayList<>();
		pedidoRepositoryInMemory.findAll().forEach(p -> listaPedidos.add(p));
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

		try {
			List<ObraDTO> obrasDTO = obraServiceImpl.getObrasByClienteParams(queryString); //Buscar obras al microservicio usuarios.
			List<Pedido> pedidosFiltrados = filtrarPedidos(listaPedidos, obrasDTO); //Filtrar por pedidos que tengan las obras traidas.
			if (!pedidosFiltrados.isEmpty()) {
				return pedidosFiltrados;
			}
			throw new PedidoNoEncontradoException("No se encontraron pedidos que cumplan con estos criterios.");
		} catch (ObraNoEncontradaException e) {
			throw e;
		}

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
		
		Optional<Pedido> ped = pedidoRepositoryInMemory.findById(idPedido);
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
}
