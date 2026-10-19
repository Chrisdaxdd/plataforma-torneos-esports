package com.esports.matchmaking.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ValidacionResultadoRequest {

	@NotBlank(message = "El nickname del árbitro que reporta/valida es obligatorio")
	private String arbitroNickname;

	@NotNull(message = "El puntaje del participante A es obligatorio")
	@Min(value = 0, message = "El puntaje no puede ser negativo")
	private Integer puntajeA;

	@NotNull(message = "El puntaje del participante B es obligatorio")
	@Min(value = 0, message = "El puntaje no puede ser negativo")
	private Integer puntajeB;

	private String incidencias;

	private boolean validarOficialmente = true;

	public ValidacionResultadoRequest() {
	}

	public String getArbitroNickname() {
		return arbitroNickname;
	}

	public void setArbitroNickname(String arbitroNickname) {
		this.arbitroNickname = arbitroNickname;
	}

	public Integer getPuntajeA() {
		return puntajeA;
	}

	public void setPuntajeA(Integer puntajeA) {
		this.puntajeA = puntajeA;
	}

	public Integer getPuntajeB() {
		return puntajeB;
	}

	public void setPuntajeB(Integer puntajeB) {
		this.puntajeB = puntajeB;
	}

	public String getIncidencias() {
		return incidencias;
	}

	public void setIncidencias(String incidencias) {
		this.incidencias = incidencias;
	}

	public boolean isValidarOficialmente() {
		return validarOficialmente;
	}

	public void setValidarOficialmente(boolean validarOficialmente) {
		this.validarOficialmente = validarOficialmente;
	}
}
