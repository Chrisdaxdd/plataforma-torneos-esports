package com.esports.torneos.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Document(collection = "usuarios")
public class Usuario {

	@Id
	private String id;

	@NotBlank(message = "El nickname es obligatorio")
	private String nickname;

	@NotBlank(message = "El nombre completo es obligatorio")
	private String nombreCompleto;

	@NotBlank(message = "El correo electrónico es obligatorio")
	@Email(message = "Debe proporcionar un correo electrónico válido")
	private String email;

	/**
	 * Roles funcionales según documento de especificación:
	 * ADMINISTRADOR, ORGANIZADOR, JUGADOR, COACH, ARBITRO
	 */
	@NotBlank(message = "El rol funcional es obligatorio")
	private String rol;

	private String equipoNombre;

	private List<String> jugadoresConfirmados = new ArrayList<>();

	private boolean activo = true;

	public Usuario() {
	}

	public Usuario(String nickname, String nombreCompleto, String email, String rol, String equipoNombre,
			List<String> jugadoresConfirmados) {
		this.nickname = nickname;
		this.nombreCompleto = nombreCompleto;
		this.email = email;
		this.rol = rol;
		this.equipoNombre = equipoNombre;
		if (jugadoresConfirmados != null) {
			this.jugadoresConfirmados = jugadoresConfirmados;
		}
		this.activo = true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getEquipoNombre() {
		return equipoNombre;
	}

	public void setEquipoNombre(String equipoNombre) {
		this.equipoNombre = equipoNombre;
	}

	public List<String> getJugadoresConfirmados() {
		return jugadoresConfirmados;
	}

	public void setJugadoresConfirmados(List<String> jugadoresConfirmados) {
		this.jugadoresConfirmados = jugadoresConfirmados;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
}
