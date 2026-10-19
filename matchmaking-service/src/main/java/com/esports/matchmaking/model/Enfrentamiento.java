package com.esports.matchmaking.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Document(collection = "enfrentamientos")
public class Enfrentamiento {

	@Id
	private String id;

	@NotBlank(message = "El ID del torneo es obligatorio")
	private String torneoId;

	private String nombreTorneo;

	@Min(value = 1, message = "El número de ronda debe ser mayor o igual a 1")
	private int ronda = 1;

	private String nombreRonda = "RONDA_1";

	@Min(value = 1, message = "El número de llave debe ser mayor o igual a 1")
	private int numeroLlave = 1;

	@NotBlank(message = "El participante A es obligatorio")
	private String participanteA;

	@NotBlank(message = "El participante B es obligatorio")
	private String participanteB;

	private List<String> integrantesParticipanteA = new ArrayList<>();

	private List<String> integrantesParticipanteB = new ArrayList<>();

	@NotBlank(message = "El nickname del árbitro asignado es obligatorio")
	private String arbitroNickname;

	private String horarioProgramado;

	private Integer puntajeA;

	private Integer puntajeB;

	private String ganador;

	/**
	 * Regla de negocio: solo un resultado validado oficialmente por el árbitro produce avance de ronda.
	 */
	private boolean resultadoValidado = false;

	private String incidencias;

	/**
	 * Estados posibles: PENDIENTE_RIVAL, PROGRAMADO, RESULTADO_REGISTRADO, VALIDADO_OFICIAL
	 */
	private String estado = "PROGRAMADO";

	private LocalDateTime fechaValidacion;

	public Enfrentamiento() {
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

	public String getNombreTorneo() {
		return nombreTorneo;
	}

	public void setNombreTorneo(String nombreTorneo) {
		this.nombreTorneo = nombreTorneo;
	}

	public int getRonda() {
		return ronda;
	}

	public void setRonda(int ronda) {
		this.ronda = ronda;
	}

	public String getNombreRonda() {
		return nombreRonda;
	}

	public void setNombreRonda(String nombreRonda) {
		this.nombreRonda = nombreRonda;
	}

	public int getNumeroLlave() {
		return numeroLlave;
	}

	public void setNumeroLlave(int numeroLlave) {
		this.numeroLlave = numeroLlave;
	}

	public String getParticipanteA() {
		return participanteA;
	}

	public void setParticipanteA(String participanteA) {
		this.participanteA = participanteA;
	}

	public String getParticipanteB() {
		return participanteB;
	}

	public void setParticipanteB(String participanteB) {
		this.participanteB = participanteB;
	}

	public List<String> getIntegrantesParticipanteA() {
		return integrantesParticipanteA;
	}

	public void setIntegrantesParticipanteA(List<String> integrantesParticipanteA) {
		this.integrantesParticipanteA = integrantesParticipanteA;
	}

	public List<String> getIntegrantesParticipanteB() {
		return integrantesParticipanteB;
	}

	public void setIntegrantesParticipanteB(List<String> integrantesParticipanteB) {
		this.integrantesParticipanteB = integrantesParticipanteB;
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

	public String getGanador() {
		return ganador;
	}

	public void setGanador(String ganador) {
		this.ganador = ganador;
	}

	public boolean isResultadoValidado() {
		return resultadoValidado;
	}

	public void setResultadoValidado(boolean resultadoValidado) {
		this.resultadoValidado = resultadoValidado;
	}

	public String getIncidencias() {
		return incidencias;
	}

	public void setIncidencias(String incidencias) {
		this.incidencias = incidencias;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public LocalDateTime getFechaValidacion() {
		return fechaValidacion;
	}

	public void setFechaValidacion(LocalDateTime fechaValidacion) {
		this.fechaValidacion = fechaValidacion;
	}
}
