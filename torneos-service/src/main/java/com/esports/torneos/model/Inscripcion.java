package com.esports.torneos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

@Document(collection = "inscripciones")
public class Inscripcion {

	@Id
	private String id;

	@NotBlank(message = "El ID del torneo es obligatorio")
	private String torneoId;

	@NotBlank(message = "El nombre del participante o equipo es obligatorio")
	private String nombreParticipante;

	/**
	 * INDIVIDUAL o EQUIPO
	 */
	@NotBlank(message = "El tipo de participación es obligatorio (INDIVIDUAL o EQUIPO)")
	private String tipoParticipacion;

	@NotBlank(message = "El nickname del solicitante (Jugador o Coach) es obligatorio")
	private String solicitanteNickname;

	/**
	 * JUGADOR o COACH
	 */
	@NotBlank(message = "El rol del solicitante es obligatorio (JUGADOR o COACH)")
	private String rolSolicitante;

	private String coachNickname;

	private List<String> plantillaJugadores = new ArrayList<>();

	/**
	 * Estados posibles: PENDIENTE, APROBADA, RECHAZADA
	 */
	private String estado = "APROBADA";

	private LocalDateTime fechaInscripcion = LocalDateTime.now();

	public Inscripcion() {
	}

	public Inscripcion(String torneoId, String nombreParticipante, String tipoParticipacion, String solicitanteNickname,
			String rolSolicitante, String coachNickname, List<String> plantillaJugadores, String estado) {
		this.torneoId = torneoId;
		this.nombreParticipante = nombreParticipante;
		this.tipoParticipacion = tipoParticipacion;
		this.solicitanteNickname = solicitanteNickname;
		this.rolSolicitante = rolSolicitante;
		this.coachNickname = coachNickname;
		if (plantillaJugadores != null) {
			this.plantillaJugadores = plantillaJugadores;
		}
		this.estado = estado != null ? estado : "APROBADA";
		this.fechaInscripcion = LocalDateTime.now();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTorneoId() {
		return torneoId;
	}

	public void setTorneoId(String torneoId) {
		this.torneoId = torneoId;
	}

	public String getNombreParticipante() {
		return nombreParticipante;
	}

	public void setNombreParticipante(String nombreParticipante) {
		this.nombreParticipante = nombreParticipante;
	}

	public String getTipoParticipacion() {
		return tipoParticipacion;
	}

	public void setTipoParticipacion(String tipoParticipacion) {
		this.tipoParticipacion = tipoParticipacion;
	}

	public String getSolicitanteNickname() {
		return solicitanteNickname;
	}

	public void setSolicitanteNickname(String solicitanteNickname) {
		this.solicitanteNickname = solicitanteNickname;
	}

	public String getRolSolicitante() {
		return rolSolicitante;
	}

	public void setRolSolicitante(String rolSolicitante) {
		this.rolSolicitante = rolSolicitante;
	}

	public String getCoachNickname() {
		return coachNickname;
	}

	public void setCoachNickname(String coachNickname) {
		this.coachNickname = coachNickname;
	}

	public List<String> getPlantillaJugadores() {
		return plantillaJugadores;
	}

	public void setPlantillaJugadores(List<String> plantillaJugadores) {
		this.plantillaJugadores = plantillaJugadores;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public LocalDateTime getFechaInscripcion() {
		return fechaInscripcion;
	}

	public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
		this.fechaInscripcion = fechaInscripcion;
	}
}
