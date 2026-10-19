package com.esports.torneos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Document(collection = "torneos")
public class Torneo {

	@Id
	private String id;

	@NotBlank(message = "El nombre del torneo es obligatorio")
	private String nombre;

	@NotBlank(message = "El videojuego es obligatorio")
	private String videojuego;

	/**
	 * Modalidad de competencia: INDIVIDUAL o EQUIPOS
	 */
	@NotBlank(message = "La modalidad es obligatoria (INDIVIDUAL o EQUIPOS)")
	private String modalidad;

	/**
	 * Formato del torneo: siempre ELIMINACION_DIRECTA según alcance del documento
	 */
	private String formato = "ELIMINACION_DIRECTA";

	@Min(value = 2, message = "El cupo máximo debe ser al menos de 2 participantes")
	private int cupoMaximo = 8;

	private int cuposOcupados = 0;

	@NotBlank(message = "Las reglas del torneo son obligatorias")
	private String reglas;

	@NotBlank(message = "El nickname del organizador es obligatorio")
	private String organizadorNickname;

	/**
	 * Estados posibles: INSCRIPCIONES_ABIERTAS, EN_CURSO, FINALIZADO
	 */
	private String estado = "INSCRIPCIONES_ABIERTAS";

	private String fechaInicio;

	public Torneo() {
	}

	public Torneo(String nombre, String videojuego, String modalidad, int cupoMaximo, String reglas,
			String organizadorNickname, String fechaInicio) {
		this.nombre = nombre;
		this.videojuego = videojuego;
		this.modalidad = modalidad;
		this.formato = "ELIMINACION_DIRECTA";
		this.cupoMaximo = cupoMaximo;
		this.cuposOcupados = 0;
		this.reglas = reglas;
		this.organizadorNickname = organizadorNickname;
		this.estado = "INSCRIPCIONES_ABIERTAS";
		this.fechaInicio = fechaInicio;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getVideojuego() {
		return videojuego;
	}

	public void setVideojuego(String videojuego) {
		this.videojuego = videojuego;
	}

	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad(String modalidad) {
		this.modalidad = modalidad;
	}

	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}

	public int getCupoMaximo() {
		return cupoMaximo;
	}

	public void setCupoMaximo(int cupoMaximo) {
		this.cupoMaximo = cupoMaximo;
	}

	public int getCuposOcupados() {
		return cuposOcupados;
	}

	public void setCuposOcupados(int cuposOcupados) {
		this.cuposOcupados = cuposOcupados;
	}

	public String getReglas() {
		return reglas;
	}

	public void setReglas(String reglas) {
		this.reglas = reglas;
	}

	public String getOrganizadorNickname() {
		return organizadorNickname;
	}

	public void setOrganizadorNickname(String organizadorNickname) {
		this.organizadorNickname = organizadorNickname;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}
}
