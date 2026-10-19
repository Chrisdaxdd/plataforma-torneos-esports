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

import com.esports.torneos.model.Inscripcion;
import com.esports.torneos.model.Torneo;
import com.esports.torneos.repository.InscripcionRepository;
import com.esports.torneos.repository.TorneoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/torneos")
@CrossOrigin(origins = "*")
@Tag(name = "Torneos", description = "Gestión directa de torneos de eliminación directa, reglas y control de cupos")
public class TorneoController {

	private static final Set<String> MODALIDADES_VALIDAS = Set.of("INDIVIDUAL", "EQUIPOS");
	private static final Set<Integer> CUPOS_ELIMINACION_DIRECTA = Set.of(2, 4, 8, 16, 32);

	private final TorneoRepository torneoRepository;
	private final InscripcionRepository inscripcionRepository;

	public TorneoController(TorneoRepository torneoRepository, InscripcionRepository inscripcionRepository) {
		this.torneoRepository = torneoRepository;
		this.inscripcionRepository = inscripcionRepository;
	}

	@GetMapping
	@Operation(summary = "Listar todos los torneos o filtrar por estado/modalidad")
	public ResponseEntity<List<Torneo>> listarTorneos(
			@RequestParam(name = "estado", required = false) String estado,
			@RequestParam(name = "modalidad", required = false) String modalidad) {
		if (estado != null && !estado.isBlank()) {
			return ResponseEntity.ok(torneoRepository.findByEstadoIgnoreCase(estado.trim()));
		}
		if (modalidad != null && !modalidad.isBlank()) {
			return ResponseEntity.ok(torneoRepository.findByModalidadIgnoreCase(modalidad.trim()));
		}
		return ResponseEntity.ok(torneoRepository.findAll());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar detalle de un torneo por ID")
	public ResponseEntity<?> obtenerTorneo(@PathVariable("id") String id) {
		Optional<Torneo> torneoOpt = torneoRepository.findById(id);
		if (torneoOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Torneo no encontrado con ID: " + id));
		}
		return ResponseEntity.ok(torneoOpt.get());
	}

	@PostMapping
	@Operation(summary = "Crear un torneo de eliminación directa (Rol Organizador)")
	public ResponseEntity<?> crearTorneo(@Valid @RequestBody Torneo torneo) {
		String modalidadNormalizada = torneo.getModalidad().trim().toUpperCase();
		if (!MODALIDADES_VALIDAS.contains(modalidadNormalizada)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Modalidad inválida. Use INDIVIDUAL o EQUIPOS"));
		}

		if (!CUPOS_ELIMINACION_DIRECTA.contains(torneo.getCupoMaximo())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Para eliminación directa el cupo máximo debe ser potencia de 2 (2, 4, 8, 16 o 32)"));
		}

		torneo.setModalidad(modalidadNormalizada);
		torneo.setFormato("ELIMINACION_DIRECTA");
		torneo.setCuposOcupados(0);
		if (torneo.getEstado() == null || torneo.getEstado().isBlank()) {
			torneo.setEstado("INSCRIPCIONES_ABIERTAS");
		} else {
			torneo.setEstado(torneo.getEstado().trim().toUpperCase());
		}

		Torneo guardado = torneoRepository.save(torneo);
		return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
	}

	@PutMapping("/{id}/estado")
	@Operation(summary = "Actualizar estado del torneo (INSCRIPCIONES_ABIERTAS, EN_CURSO, FINALIZADO)")
	public ResponseEntity<?> actualizarEstado(
			@PathVariable("id") String id,
			@RequestParam(name = "nuevoEstado") String nuevoEstado) {
		Optional<Torneo> torneoOpt = torneoRepository.findById(id);
		if (torneoOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Torneo no encontrado con ID: " + id));
		}
		Torneo torneo = torneoOpt.get();
		torneo.setEstado(nuevoEstado.trim().toUpperCase());
		return ResponseEntity.ok(torneoRepository.save(torneo));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar un torneo y sus inscripciones asociadas")
	public ResponseEntity<?> eliminarTorneo(@PathVariable("id") String id) {
		if (!torneoRepository.existsById(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Torneo no encontrado con ID: " + id));
		}
		List<Inscripcion> inscripciones = inscripcionRepository.findByTorneoId(id);
		if (!inscripciones.isEmpty()) {
			inscripcionRepository.deleteAll(inscripciones);
		}
		torneoRepository.deleteById(id);
		return ResponseEntity.ok(Map.of("mensaje", "Torneo e inscripciones eliminados correctamente"));
	}
}
