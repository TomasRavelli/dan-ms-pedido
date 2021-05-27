package dan.tp2021.pedidos.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import dan.tp2021.pedidos.dao.EstadoPedidoRepository;
import dan.tp2021.pedidos.dao.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.EstadoPedido;
import dan.tp2021.pedidos.domain.Obra;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.domain.Producto;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.exceptions.cliente.ClienteNoHabilitadoException;
import dan.tp2021.pedidos.services.BancoService;
import dan.tp2021.pedidos.services.ClienteService;
import dan.tp2021.pedidos.services.EstadoPedidoService;
import dan.tp2021.pedidos.services.PedidoService;

@SpringBootTest
class PedidoServiceUnitTest {

	@Autowired
	PedidoService pedidoService;

	@MockBean
	PedidoRepository pedidoRepo;

	@MockBean
	EstadoPedidoService estadoPedidoService;

	@MockBean
	ClienteService clienteService;

	ClienteDTO clienteDTO;

	@MockBean
	BancoService bancoService;

	Pedido unPedido;

	@BeforeEach
	void setUp() {
		unPedido = new Pedido();
		Obra obra = new Obra();
		Producto p = new Producto();
		p.setStockActual(20);
		DetallePedido d1 = new DetallePedido(p, 5, 40.0);
		DetallePedido d2 = new DetallePedido(p, 10, 80.0);
		DetallePedido d3 = new DetallePedido(p, 2, 450.0);
		unPedido.setDetalle(new ArrayList<DetallePedido>());
		unPedido.getDetalle().add(d1);
		unPedido.getDetalle().add(d2);
		unPedido.getDetalle().add(d3);
		unPedido.setObra(obra);
		// total pedido 40*5 + 80*10 + 450*2 = 200+800+900= 1900. No. El precio que le
		// seteas ya es el total, cada producto tiene el precio unitario.
		// Total pedido es 40+80+450 = 570
		clienteDTO = new ClienteDTO();
		clienteDTO.setSaldoActual(2500d);
		clienteDTO.setMaxCuentaOnline(5000d);
	}

	@Test
	void testCrearPedidoConStockSinDeuda() throws Exception {

		// Simular la busqueda de un Cliente, devuelve el clienteDTO.
		when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
		// Simular la verificacion del cliente en el Banco.
		when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
		// Simular la busqueda de EstadoPedido a la BD.
		when(estadoPedidoService.findByEstado("ACEPTADO")).thenReturn(new EstadoPedido(5, "ACEPTADO"));
		when(estadoPedidoService.findByEstado("PENDIENTE")).thenReturn(new EstadoPedido(3, "PENDIENTE"));
		when(estadoPedidoService.findByEstado("NUEVO")).thenReturn(new EstadoPedido(1, "NUEVO"));
		// retorno el pedido
		when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);

		Pedido pedidoResultado;

		try {
			pedidoResultado = pedidoService.savePedido(unPedido);
			assertEquals("ACEPTADO", (pedidoResultado.getEstado().getEstado()));

		} catch (Exception e) {

			fail("Se lanzó un excepción en savePedido()", e);
//			assertEquals("PASO TEST", "NO PASO TEST");
//			e.printStackTrace();
		}

	}

	@Test
	void testCrearPedidoSinStockSinDeuda() throws Exception {

		when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);

		when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);

		// Simular la busqueda de EstadoPedido a la BD.
		when(estadoPedidoService.findByEstado("ACEPTADO")).thenReturn(new EstadoPedido(5, "ACEPTADO"));
		when(estadoPedidoService.findByEstado("PENDIENTE")).thenReturn(new EstadoPedido(3, "PENDIENTE"));
		when(estadoPedidoService.findByEstado("NUEVO")).thenReturn(new EstadoPedido(1, "NUEVO"));
		// retorno el pedido
		when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);

		// Creo un pedido auxiliar, que le agrego un producto sin stock.

		Producto productoSinStock = new Producto();
		productoSinStock.setStockActual(0);
		DetallePedido d4 = new DetallePedido(productoSinStock, 2, 450.0);
		unPedido.getDetalle().add(d4);

		Pedido pedidoResultado;
		try {
			pedidoResultado = pedidoService.savePedido(unPedido);
			assertEquals("PENDIENTE", pedidoResultado.getEstado().getEstado());
		} catch (Exception e) {
			fail("Se lanzó una excepción en savePedido()", e);
		}

	}

	@Test
	void testCrearPedidoConStockSinDeudaConPerfilBancarioMalo() throws Exception {

		// Al clienteDTO le seteo un saldo menor a la compra que hizo

		when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
		when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(false);
		// Simular la busqueda de EstadoPedido a la BD.
		when(estadoPedidoService.findByEstado("ACEPTADO")).thenReturn(new EstadoPedido(5, "ACEPTADO"));
		when(estadoPedidoService.findByEstado("PENDIENTE")).thenReturn(new EstadoPedido(3, "PENDIENTE"));
		when(estadoPedidoService.findByEstado("NUEVO")).thenReturn(new EstadoPedido(1, "NUEVO"));
		// retorno el pedido
		when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);

		try {

			Pedido resultado = pedidoService.savePedido(unPedido);
			assertEquals("ACEPTADO", (resultado.getEstado().getEstado()));
		} catch (Exception e) {
			fail("Se lanzó una excepción en savePedido()", e);
		}

	}

	@Test
	void testCrearPedidoConStockConDeudaConPerfilBancarioMalo() throws Exception {

		// Al clienteDTO le seteo un saldo menor a la compra que hizo
		clienteDTO.setSaldoActual(100d);

		when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
		when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(false);
		// Simular la busqueda de EstadoPedido a la BD.
		when(estadoPedidoService.findByEstado("ACEPTADO")).thenReturn(new EstadoPedido(5, "ACEPTADO"));
		when(estadoPedidoService.findByEstado("PENDIENTE")).thenReturn(new EstadoPedido(3, "PENDIENTE"));
		when(estadoPedidoService.findByEstado("NUEVO")).thenReturn(new EstadoPedido(1, "NUEVO"));
		// retorno el pedido
		when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);

		ClienteNoHabilitadoException exception = assertThrows(ClienteNoHabilitadoException.class,
				() -> pedidoService.savePedido(unPedido));

		assertEquals(exception.getMessage(), "Error. El cliente no cumple con las condiciones para adquirir el pedido");
//		try {
//
//			Pedido resultado = pedidoService.savePedido(unPedido);
//			assertEquals("DEBERIA ENTRAR AL CATCH", "DEBE FALLAR");
////			assertEquals("ACEPTADO",((Pedido)pedidoResultado.getBody()).getEstado().getEstado());
//		} catch (Exception e) {
//			assertEquals(e.getMessage(), "Error. El cliente no cumple con las condiciones para adquirir el pedido");
//		}

	}

	@Test
	void testCrearPedidoConStockConDeudaPerfilBueno() throws Exception {

		// Al clienteDTO le seteo un saldo menor a la compra que hizo
		clienteDTO.setSaldoActual(200d);
		when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
		when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);

		// Simular la busqueda de EstadoPedido a la BD.
		when(estadoPedidoService.findByEstado("ACEPTADO")).thenReturn(new EstadoPedido(5, "ACEPTADO"));
		when(estadoPedidoService.findByEstado("PENDIENTE")).thenReturn(new EstadoPedido(3, "PENDIENTE"));
		when(estadoPedidoService.findByEstado("NUEVO")).thenReturn(new EstadoPedido(1, "NUEVO"));

		// retorno el pedido
		when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);

		try {
			Pedido pedidoResultado = pedidoService.savePedido(unPedido);
			System.out.println((pedidoResultado.getEstado().getEstado()));
			assertEquals("ACEPTADO", (pedidoResultado.getEstado().getEstado()));
		} catch (Exception e) {
			fail("Se lanzó una excepción en savePedido()", e);
		}

	}

	@Test
	void testCrearPedidoConStockConDeudaMayorAlDecubiertoPerfilBueno() throws Exception {

		// Al clienteDTO le seteo un saldo menor a la compra que hizo
		clienteDTO.setSaldoActual(200d);
		clienteDTO.setMaxCuentaOnline(250d);
		when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
		when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);

		// Simular la busqueda de EstadoPedido a la BD.
		when(estadoPedidoService.findByEstado("ACEPTADO")).thenReturn(new EstadoPedido(5, "ACEPTADO"));
		when(estadoPedidoService.findByEstado("PENDIENTE")).thenReturn(new EstadoPedido(3, "PENDIENTE"));
		when(estadoPedidoService.findByEstado("NUEVO")).thenReturn(new EstadoPedido(1, "NUEVO"));
		// retorno el pedido
		when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);

		ClienteNoHabilitadoException exception = assertThrows(ClienteNoHabilitadoException.class,
				() -> pedidoService.savePedido(unPedido));

		assertEquals(exception.getMessage(), "Error. El cliente no cumple con las condiciones para adquirir el pedido");
//		try {
//			Pedido pedidoResultado = pedidoService.savePedido(unPedido);
//
//			assertEquals("DEBERIA ENTRAR","AL CATCH");
//		} catch (Exception e) {
//			assertEquals(e.getMessage(), "Error. El cliente no cumple con las condiciones para adquirir el pedido");
//		}

	}

}
