package com.esports.matchmaking.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class GeneracionCuadroRequest {

	@NotBlank(message = "El ID del torneo es obligatorio")
	private String torneoId;

	private String nombreTorneo;

	@NotEmpty(message = "Se requiere la lista de participantes con inscripción aprobada")
	private List<String> participantesAprobados = new ArrayList<>();

	@NotBlank(message = "El nickname del árbitro asignado es obligatorio")
	private String arbitroNickname;

	private String horarioProgramado;

	public GeneracionCuadroRequest() {
	}

	public String getTorneoId() {
		return torneoId;
	}

	public void setTorneoId(String torneoId) {
		this.torneoId = torneoId;
	}

	public String getNombreTorneo() {
		return nombreTorneo;
	}

	public void setNombreTorneo(String nombreTorneo) {
		this.nombreTorneo = nombreTorneo;
	}

	public List<String> getParticipantesAprobados() {
		return participantesAprobados;
	}

	public void setParticipantesAprobados(List<String> participantesAprobados) {
		this.participantesAprobados = participantesAprobados;
	}

	public String getArbitroNickname() {
		return arbitroNickname;
	}

	public void setArbitroNickname(String arbitroNickname) {
		this.arbitroNickname = arbitroNickname;
	}

	public String getHorarioProgramado() {
		return horarioProgramado;
	}

	public void setHorarioProgramado(String horarioProgramado) {
		this.horarioProgramado = horarioProgramado;
	}
}
