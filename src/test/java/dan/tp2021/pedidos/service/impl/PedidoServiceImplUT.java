package dan.tp2021.pedidos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.Obra;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.domain.Producto;
import dan.tp2021.pedidos.repository.PedidoRepository;
import dan.tp2021.pedidos.service.ClienteService;
import dan.tp2021.pedidos.service.MaterialService;
import dan.tp2021.pedidos.service.PedidoServiceProfe;

@SpringBootTest
class PedidoServiceImplUT {
	
	@Autowired
	PedidoServiceProfe pedidoService;
	
	@MockBean
	PedidoRepository pedidoRepo;
	
	@MockBean
	ClienteService clienteService;

	@MockBean
	MaterialService materialService;
	
	Pedido unPedido;

	@BeforeEach
	void setUp() throws Exception {
		unPedido = new Pedido();
		Obra obra = new Obra();
		DetallePedido d1 = new DetallePedido(new Producto(),5,40.0);
		DetallePedido d2 = new DetallePedido(new Producto(),10,80.0);
		DetallePedido d3 = new DetallePedido(new Producto(),2,450.0);
		unPedido.setDetalle(new ArrayList<DetallePedido>());
		unPedido.getDetalle().add(d1);
		unPedido.getDetalle().add(d2);
		unPedido.getDetalle().add(d3);
		unPedido.setObra(obra);
		// 	total pedido 40*5 + 80*10 + 450*2 = 200+800+900= 1900
	}

	@Test
	void testCrearPedidoConStockSinDeuda() {
		// siempre hay stock
		when(materialService.stockDisponible(any(Producto.class))).thenReturn(20);
		// el cliente no tiene deuda
		when(clienteService.deudaCliente(any(Obra.class))).thenReturn(0.0);
		// el saldo negativo maximo es 10000
		when(clienteService.maximoSaldoNegativo(any(Obra.class))).thenReturn(10000.0);
		// el saldo negativo maximo es 10000
		when(clienteService.situacionCrediticiaBCRA(any(Obra.class))).thenReturn(1);
		// retorno el pedido
		when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);

		Pedido pedidoResultado = pedidoService.crearPedido(unPedido);
		assertThat(pedidoResultado.getEstado().getId().equals(1));
	}

	@Test
	@Disabled("pendiente")
	void testCrearPedidoSinStockSinDeuda() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled("pendiente")
	void testVerificarStock() {
		fail("Not yet implemented");
	}

	public void hacerAlgo() {
		System.out.println("hace algo");
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		throw new IndexOutOfBoundsException(-99);
	}
	
//	@Test
//	@Disabled("pendiente")
//	void testEsDeBajoRiesgo() {
//		fail("Not yet implemented");
//		
//		Throwable exception = assertThrows( 
//				IndexOutOfBoundsException.class, 
//			              () -> hacerAlgo()
//			); 
//			assertEquals("Exception message", exception.getMessage()); 
//	}
//
//	@Test
//	public void whenAssertingTimeout_thenNotExceeded() {
//	    assertTimeout(
//	      Duration.ofSeconds(2), 
//	      () -> hacerAlgo()
//	    );
//	}
//	
//	@Test
//	void verificarAll() {
//	  Producto p= new Producto();
//	  p.setDescripcion("PRoducto1");
//	  p.setPrecio(100.0);
//	  assertAll("producto", 
//	                () -> assertEquals("PRoducto1", p.getDescripcion()),
//	                () -> assertEquals(100.0, p.getPrecio())
//	            );
//	  }

	


	@AfterEach
	void tearDown() throws Exception {
	}
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

}
