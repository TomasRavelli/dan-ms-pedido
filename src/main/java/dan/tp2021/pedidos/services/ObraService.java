package dan.tp2021.pedidos.services;

import java.util.List;

import dan.tp2021.pedidos.dto.ObraDTO;

public interface ObraService {

	public static class ObraNoEncontradaException extends Exception { ObraNoEncontradaException(String message){super(message);}}
	public List<ObraDTO> getObrasByClienteParams(String s) throws ObraNoEncontradaException;
}
