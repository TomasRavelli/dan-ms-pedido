package dan.tp2021.pedidos.dto;

import java.time.Instant;


public class ClienteDTO {

	private Integer id;
    private String razonSocial;
    private String cuit;
    private String mail;
    private Double maxCuentaOnline;
    private Double saldoActual;
    //No hace falta el habilitado, cada vez que se necesite saber la situacion el sistema se comunicaria
    //con el sistema de BCRA
    private Boolean habilitadoOnline;
    private Instant fechaBaja;
	
    public ClienteDTO() {
		super();
		
	}
    


	public ClienteDTO(Integer id, String razonSocial, String cuit, String mail, Double maxCuentaOnline,
					  Double saldoActual, Boolean habilitadoOnline, Instant fechaBaja) {
		super();
		this.id = id;
		this.razonSocial = razonSocial;
		this.cuit = cuit;
		this.mail = mail;
		this.maxCuentaOnline = maxCuentaOnline;
		this.saldoActual = saldoActual;
		this.habilitadoOnline = habilitadoOnline;
		this.fechaBaja = fechaBaja;
	}




	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getCuit() {
		return cuit;
	}

	public void setCuit(String cuit) {
		this.cuit = cuit;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Double getMaxCuentaOnline() {
		return maxCuentaOnline;
	}

	public void setMaxCuentaOnline(Double maxCuentaOnline) {
		this.maxCuentaOnline = maxCuentaOnline;
	}

	public Boolean getHabilitadoOnline() {
		return habilitadoOnline;
	}

	public void setHabilitadoOnline(Boolean habilitadoOnline) {
		this.habilitadoOnline = habilitadoOnline;
	}



	public Double getSaldoActual() {
		return saldoActual;
	}



	public void setSaldoActual(Double saldoActual) {
		this.saldoActual = saldoActual;
	}



	public Instant getFechaBaja() {
		return fechaBaja;
	}



	public void setFechaBaja(Instant fechaBaja) {
		this.fechaBaja = fechaBaja;
	}

    
}


