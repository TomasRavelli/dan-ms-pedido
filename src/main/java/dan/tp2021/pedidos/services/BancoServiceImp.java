package dan.tp2021.pedidos.services;

import org.springframework.stereotype.Service;

import dan.tp2021.pedidos.dto.ClienteDTO;

@Service
public class BancoServiceImp implements BancoService {

	@Override
	public Boolean verificarSituacionCliente(ClienteDTO c) {
		
		return true;
	}

}
