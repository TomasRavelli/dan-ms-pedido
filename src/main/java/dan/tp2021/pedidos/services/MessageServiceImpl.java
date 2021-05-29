package dan.tp2021.pedidos.services;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import dan.tp2021.pedidos.domain.DetallePedido;
import dan.tp2021.pedidos.domain.Pedido;

@Service
public class MessageServiceImpl implements MessageService {

	private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
 

	@Autowired
	JmsTemplate jms;
	
	@Override
	public void sendMessageToProductos(Pedido p) {
		
		logger.debug("PEDIDO QUE LLEGO PARA ENVIAR EL MENSAJE: "+p.toString());
		ArrayList<Integer> idDetalles = new ArrayList<>();
		for(DetallePedido d: p.getDetalle()){
			idDetalles.add(d.getId());
		}
		jms.convertAndSend("COLA_PEDIDOS",idDetalles);
	}

}
