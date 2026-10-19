package com.esports.torneos.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esports.torneos.model.Usuario;
import com.esports.torneos.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@Tag(name = "Usuarios y Roles", description = "Gestión directa de perfiles y los 5 roles funcionales (ADMINISTRADOR, ORGANIZADOR, JUGADOR, COACH, ARBITRO)")
public class UsuarioController {

	private static final Set<String> ROLES_VALIDOS = Set.of(
			"ADMINISTRADOR", "ORGANIZADOR", "JUGADOR", "COACH", "ARBITRO"
	);

	private final UsuarioRepository usuarioRepository;

	public UsuarioController(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@GetMapping
	@Operation(summary = "Listar usuarios registrados o filtrar por rol funcional")
	public ResponseEntity<List<Usuario>> listarUsuarios(
			@RequestParam(name = "rol", required = false) String rol) {
		if (rol != null && !rol.isBlank()) {
			return ResponseEntity.ok(usuarioRepository.findByRolIgnoreCase(rol.trim().toUpperCase()));
		}
		return ResponseEntity.ok(usuarioRepository.findAll());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar perfil de usuario por ID")
	public ResponseEntity<?> obtenerPorId(@PathVariable("id") String id) {
		Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
		if (usuarioOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Usuario no encontrado con ID: " + id));
		}
		return ResponseEntity.ok(usuarioOpt.get());
	}

	@PostMapping
	@Operation(summary = "Registrar nuevo usuario validando rol y unicidad de nickname/email")
	public ResponseEntity<?> registrarUsuario(@Valid @RequestBody Usuario usuario) {
		String rolNormalizado = usuario.getRol().trim().toUpperCase();
		if (!ROLES_VALIDOS.contains(rolNormalizado)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Rol inválido. Roles permitidos: " + ROLES_VALIDOS));
		}
		if (usuarioRepository.existsByNicknameIgnoreCase(usuario.getNickname().trim())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Ya existe un usuario con el nickname: " + usuario.getNickname()));
		}
		if (usuarioRepository.existsByEmailIgnoreCase(usuario.getEmail().trim())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Ya existe un usuario con el correo: " + usuario.getEmail()));
		}

		usuario.setNickname(usuario.getNickname().trim());
		usuario.setEmail(usuario.getEmail().trim());
		usuario.setRol(rolNormalizado);
		Usuario guardado = usuarioRepository.save(usuario);
		return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar perfil, rol o plantilla de equipo de un usuario")
	public ResponseEntity<?> actualizarUsuario(@PathVariable("id") String id, @Valid @RequestBody Usuario datos) {
		Optional<Usuario> existenteOpt = usuarioRepository.findById(id);
		if (existenteOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Usuario no encontrado con ID: " + id));
		}

		String rolNormalizado = datos.getRol().trim().toUpperCase();
		if (!ROLES_VALIDOS.contains(rolNormalizado)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Rol inválido. Roles permitidos: " + ROLES_VALIDOS));
		}

		Usuario usuario = existenteOpt.get();
		usuario.setNickname(datos.getNickname().trim());
		usuario.setNombreCompleto(datos.getNombreCompleto().trim());
		usuario.setEmail(datos.getEmail().trim());
		usuario.setRol(rolNormalizado);
		usuario.setEquipoNombre(datos.getEquipoNombre());
		usuario.setJugadoresConfirmados(datos.getJugadoresConfirmados());
		usuario.setActivo(datos.isActivo());

		return ResponseEntity.ok(usuarioRepository.save(usuario));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar usuario por ID (Rol Administrador)")
	public ResponseEntity<?> eliminarUsuario(@PathVariable("id") String id) {
		if (!usuarioRepository.existsById(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Usuario no encontrado con ID: " + id));
		}
		usuarioRepository.deleteById(id);
		return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente"));
	}
}
