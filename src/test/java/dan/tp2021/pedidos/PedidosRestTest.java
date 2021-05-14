package dan.tp2021.pedidos;

import dan.tp2021.pedidos.dao.PedidoRepositoryInMemory;
import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.Obra;
import dan.tp2021.pedidos.domain.Pedido;
import dan.tp2021.pedidos.domain.Producto;
import dan.tp2021.pedidos.dto.ClienteDTO;
import dan.tp2021.pedidos.exceptions.cliente.ClienteException;
import dan.tp2021.pedidos.exceptions.cliente.ClienteNoHabilitadoException;
import dan.tp2021.pedidos.exceptions.pedido.PedidoNoEncontradoException;
import dan.tp2021.pedidos.repository.PedidoRepository;
import dan.tp2021.pedidos.services.BancoService;
import dan.tp2021.pedidos.services.ClienteService;
import dan.tp2021.pedidos.services.PedidoService;
import dan.tp2021.pedidos.services.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//@SpringBootApplication
//@Profile("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PedidosRestTest {
    private TestRestTemplate testRestTemplate = new TestRestTemplate();
    @LocalServerPort
    String puerto;

    Pedido unPedido;
    Obra unaObra;
    DetallePedido unDetallePedido, otroDetallePedido;
    Producto unProducto, otroProducto;
    ClienteDTO clienteDTO;

    /*@MockBean
    PedidoService pedidoServiceImpl;*/

    @Autowired
    PedidoService pedidoService;

    @MockBean
    PedidoRepositoryInMemory pedidoRepo;

    @MockBean
    ClienteService clienteService;

    @MockBean
    BancoService bancoService;

    @BeforeEach
    void setUp(){
        unPedido = new Pedido();
        unaObra = new Obra();
        unDetallePedido = new DetallePedido();
        otroDetallePedido = new DetallePedido();
        unProducto = new Producto();
        otroProducto = new Producto();

        unProducto.setStockActual(600);
        otroProducto.setStockActual(600);
        unPedido.setId(1);
        unPedido.setDetalle(new ArrayList<>());
        unaObra.setId(1);
        unaObra.setDescripcion("Obra de prueba");
        unDetallePedido.setProducto(unProducto);
        unDetallePedido.setCantidad(5);
        unDetallePedido.setPrecio(150.85);
        otroDetallePedido.setProducto(otroProducto);
        otroDetallePedido.setCantidad(20);
        otroDetallePedido.setPrecio(124.20);

        unPedido.setObra(unaObra);
        unPedido.getDetalle().add(unDetallePedido);
        unPedido.getDetalle().add(otroDetallePedido);


        clienteDTO = new ClienteDTO();
        clienteDTO.setSaldoActual(2500d);
        clienteDTO.setMaxCuentaOnline(5000d);

    }

    //crear pedido
    @Test
    void crearPedidoCorrecto() throws ClienteException {
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        String server = "http://localhost:"+puerto+"/api/pedido";
        HttpEntity<Pedido> requestPedido = new HttpEntity<>(unPedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.OK,response.getStatusCode());
    }

    @Test
    void crearPedidoSinObra() throws ClienteException, ClienteNoHabilitadoException {
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        unPedido.setObra(null);
        String server = "http://localhost:"+puerto+"/api/pedido";
        HttpEntity<Pedido> requestPedido = new HttpEntity<>(unPedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    void crearPedidoSinDetalles() throws ClienteException, ClienteNoHabilitadoException {
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        unPedido.setDetalle(null);
        String server = "http://localhost:"+puerto+"/api/pedido";
        HttpEntity<Pedido> requestPedido = new HttpEntity<>(unPedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    void crearPedidoConDetallesSinContenido() throws ClienteException, ClienteNoHabilitadoException {
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        unPedido.setDetalle(new ArrayList<>());
        String server = "http://localhost:"+puerto+"/api/pedido";
        HttpEntity<Pedido> requestPedido = new HttpEntity<>(unPedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    void crearPedidoConUnDetalleSinProducto() throws ClienteException, ClienteNoHabilitadoException {
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        unPedido.getDetalle().get(0).setProducto(null);
        String server = "http://localhost:"+puerto+"/api/pedido";
        HttpEntity<Pedido> requestPedido = new HttpEntity<>(unPedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    void crearPedidoConUnDetalleSinCantidad() throws ClienteException, ClienteNoHabilitadoException {
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(clienteService.getClienteByObra(any(Pedido.class))).thenReturn(clienteDTO);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        unPedido.getDetalle().get(1).setCantidad(null);
        String server = "http://localhost:"+puerto+"/api/pedido";
        HttpEntity<Pedido> requestPedido = new HttpEntity<>(unPedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    //agregar item a pedido
    @Test
    void agregarItemAUnPedidoCorrecto() throws PedidoNoEncontradoException {
        when(pedidoRepo.findById(any(Integer.class))).thenReturn(java.util.Optional.ofNullable(unPedido));
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        unPedido.getDetalle().get(1).setCantidad(null);
        String server = "http://localhost:"+puerto+"/api/pedido/"+unPedido.getId()+"/detalle";
        HttpEntity<DetallePedido> requestPedido = new HttpEntity<>(otroDetallePedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.OK,response.getStatusCode());
    }

    @Test
    void agregarItemPedidoNoEncontrado() {
        //No lo encuentra, retorna Optional.empty()
        when(pedidoRepo.findById(any(Integer.class))).thenReturn(Optional.empty());
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
        when(bancoService.verificarSituacionCliente(any(ClienteDTO.class))).thenReturn(true);
        unPedido.getDetalle().get(1).setCantidad(null);
        String server = "http://localhost:"+puerto+"/api/pedido/"+unPedido.getId()+"/detalle";
        HttpEntity<DetallePedido> requestPedido = new HttpEntity<>(otroDetallePedido);
        ResponseEntity<String> response = testRestTemplate.exchange(server, HttpMethod.POST,requestPedido,String.class);
        assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
    }
}
