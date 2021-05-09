package dan.tp2021.pedidos.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dan.tp2021.pedidos.dao.PedidoRepositoryInMemory;
import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.EstadoPedido;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.dto.ClienteDTO;

@Service
public class PedidoServiceImpl implements PedidoService {

	@Autowired
	PedidoRepositoryInMemory pedidoRepositoryInMemory;
	
	@Autowired
	ClienteService clienteServiceImpl;
	
	@Autowired
	BancoService bancoServiceImpl;

	@Override
	public Pedido savePedido(Pedido p) throws ClienteNoHabilitadoException, ClienteService.ClienteException {

		EstadoPedido estadoPedido = new EstadoPedido();

		ClienteDTO clienteDTO = clienteServiceImpl.getClienteByObra(p);
		//Si llegamos acá quiere decir que no hubo error, porque cuando hay error se lanza una excepción y la ejecución se corta.

		double sumaCostosProductos = 0;
		boolean stockDisponible = true;

		for (DetallePedido dp : p.getDetalle()) {

			sumaCostosProductos += dp.getPrecio();
			//Aca se deberia usar el materialService creo. COMENTARIO DE PERU.
			//Eso estaba implementado por el profe, si quieren lo hacemos como el. COMENTARIO TOMAS.
//				if(materialService.stockDisponible(dp.getProducto()) <= 0 && stockDisponible){
//					stockDisponible = false;
//				}
			if (dp.getProducto().getStockActual() <= 0 && stockDisponible) {

				stockDisponible = false;

			}

		}

		boolean esDeudor = sumaCostosProductos > clienteDTO.getSaldoActual(), superaDescubierto = sumaCostosProductos > clienteDTO.getMaxCuentaOnline();
		boolean condicionC = !superaDescubierto && bancoServiceImpl.verificarSituacionCliente(clienteDTO); //TODO ver como tratamos el getHabilitadoOnline

		if (!esDeudor || condicionC) {

			if (stockDisponible) {
				estadoPedido.setEstado("ACEPTADO");
			} else {
				estadoPedido.setEstado("PENDIENTE");
			}

			p.setEstado(estadoPedido);
			//TODO ver que hacer si otras implementaciones de este método lanzan excepciones.
			// Las lanzamos hacia arriba y que se encarque el controller? O la capturamos y lanzamos otra excepción más "linda"?
			return pedidoRepositoryInMemory.save(p);

		} else {
			throw new ClienteNoHabilitadoException("Error. El cliente no cumple con las condiciones para adquirir el pedido");
		}


	}
}
