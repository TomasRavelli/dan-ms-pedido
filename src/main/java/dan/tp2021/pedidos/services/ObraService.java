package dan.tp2021.pedidos.services;

import java.util.List;

import dan.tp2021.pedidos.dto.ObraDTO;
import dan.tp2021.pedidos.exceptions.obra.ObraNoEncontradaException;

public interface ObraService {
	public List<ObraDTO> getObrasByClienteParams(String s) throws ObraNoEncontradaException;
}
