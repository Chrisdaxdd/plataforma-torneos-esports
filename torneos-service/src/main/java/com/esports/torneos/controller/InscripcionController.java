package com.esports.torneos.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = "*")
@Tag(name = "Inscripciones", description = "Control estricto de inscripciones individuales (Jugador) o por equipos (Coach), evitando duplicados y sobrecupos")
public class InscripcionController {

	private final InscripcionRepository inscripcionRepository;
	private final TorneoRepository torneoRepository;

	public InscripcionController(InscripcionRepository inscripcionRepository, TorneoRepository torneoRepository) {
		this.inscripcionRepository = inscripcionRepository;
		this.torneoRepository = torneoRepository;
	}

	@GetMapping
	@Operation(summary = "Listar inscripciones por torneo y/o estado")
	public ResponseEntity<List<Inscripcion>> listarInscripciones(
			@RequestParam(name = "torneoId", required = false) String torneoId,
			@RequestParam(name = "estado", required = false) String estado) {
		if (torneoId != null && !torneoId.isBlank() && estado != null && !estado.isBlank()) {
			return ResponseEntity.ok(
					inscripcionRepository.findByTorneoIdAndEstadoIgnoreCase(torneoId.trim(), estado.trim()));
		}
		if (torneoId != null && !torneoId.isBlank()) {
			return ResponseEntity.ok(inscripcionRepository.findByTorneoId(torneoId.trim()));
		}
		return ResponseEntity.ok(inscripcionRepository.findAll());
	}

	@PostMapping
	@Operation(summary = "Registrar inscripción validando duplicados, cupo máximo y modalidad (Jugador o Coach)")
	public ResponseEntity<?> registrarInscripcion(@Valid @RequestBody Inscripcion inscripcion) {
		Optional<Torneo> torneoOpt = torneoRepository.findById(inscripcion.getTorneoId());
		if (torneoOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "El torneo especificado no existe"));
		}

		Torneo torneo = torneoOpt.get();

		if (!"INSCRIPCIONES_ABIERTAS".equalsIgnoreCase(torneo.getEstado())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "El torneo no tiene inscripciones abiertas (Estado actual: " + torneo.getEstado() + ")"));
		}

		if (torneo.getCuposOcupados() >= torneo.getCupoMaximo()) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Sobrecupo rechazado: el torneo ya alcanzó su cupo máximo de " + torneo.getCupoMaximo() + " participantes"));
		}

		if (inscripcionRepository.existsByTorneoIdAndNombreParticipanteIgnoreCase(
				torneo.getId(), inscripcion.getNombreParticipante().trim())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Inscripción duplicada: el participante o equipo '"
							+ inscripcion.getNombreParticipante() + "' ya está inscrito en este torneo"));
		}

		if (inscripcionRepository.existsByTorneoIdAndSolicitanteNicknameIgnoreCase(
				torneo.getId(), inscripcion.getSolicitanteNickname().trim())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Inscripción duplicada: el solicitante '"
							+ inscripcion.getSolicitanteNickname() + "' ya registró una inscripción en este torneo"));
		}

		String rolSolicitante = inscripcion.getRolSolicitante().trim().toUpperCase();
		String tipoParticipacion = inscripcion.getTipoParticipacion().trim().toUpperCase();

		if ("EQUIPOS".equalsIgnoreCase(torneo.getModalidad())) {
			if (!"COACH".equals(rolSolicitante) || !"EQUIPO".equals(tipoParticipacion)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "En torneos por EQUIPOS la inscripción debe ser coordinada por un COACH con tipo EQUIPO"));
			}
			if (inscripcion.getPlantillaJugadores() == null || inscripcion.getPlantillaJugadores().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "El Coach debe incluir la plantilla de jugadores confirmados del equipo"));
			}
			inscripcion.setCoachNickname(inscripcion.getSolicitanteNickname().trim());
		} else {
			if (!"JUGADOR".equals(rolSolicitante) || !"INDIVIDUAL".equals(tipoParticipacion)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "En torneos de modalidad INDIVIDUAL la inscripción debe realizarla un JUGADOR"));
			}
			if (inscripcion.getPlantillaJugadores() == null || inscripcion.getPlantillaJugadores().isEmpty()) {
				inscripcion.setPlantillaJugadores(List.of(inscripcion.getSolicitanteNickname().trim()));
			}
		}

		inscripcion.setNombreParticipante(inscripcion.getNombreParticipante().trim());
		inscripcion.setSolicitanteNickname(inscripcion.getSolicitanteNickname().trim());
		inscripcion.setRolSolicitante(rolSolicitante);
		inscripcion.setTipoParticipacion(tipoParticipacion);
		inscripcion.setFechaInscripcion(LocalDateTime.now());

		String estadoInicial = (inscripcion.getEstado() == null || inscripcion.getEstado().isBlank())
				? "APROBADA"
				: inscripcion.getEstado().trim().toUpperCase();
		inscripcion.setEstado(estadoInicial);

		Inscripcion guardada = inscripcionRepository.save(inscripcion);

		if ("APROBADA".equals(estadoInicial)) {
			torneo.setCuposOcupados(torneo.getCuposOcupados() + 1);
			torneoRepository.save(torneo);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
	}

	@PutMapping("/{id}/estado")
	@Operation(summary = "Aprobar o rechazar una inscripción por parte del Organizador verificando sobrecupo")
	public ResponseEntity<?> cambiarEstadoInscripcion(
			@PathVariable("id") String id,
			@RequestParam(name = "nuevoEstado") String nuevoEstado) {
		Optional<Inscripcion> inscOpt = inscripcionRepository.findById(id);
		if (inscOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Inscripción no encontrada con ID: " + id));
		}

		Inscripcion inscripcion = inscOpt.get();
		Optional<Torneo> torneoOpt = torneoRepository.findById(inscripcion.getTorneoId());
		if (torneoOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Torneo asociado no encontrado"));
		}

		Torneo torneo = torneoOpt.get();
		String estadoAnterior = inscripcion.getEstado();
		String estadoDestino = nuevoEstado.trim().toUpperCase();

		if ("APROBADA".equals(estadoDestino) && !"APROBADA".equalsIgnoreCase(estadoAnterior)) {
			if (torneo.getCuposOcupados() >= torneo.getCupoMaximo()) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(Map.of("error", "No se puede aprobar: el torneo ya alcanzó su cupo máximo de " + torneo.getCupoMaximo()));
			}
			torneo.setCuposOcupados(torneo.getCuposOcupados() + 1);
			torneoRepository.save(torneo);
		} else if (!"APROBADA".equals(estadoDestino) && "APROBADA".equalsIgnoreCase(estadoAnterior)) {
			torneo.setCuposOcupados(Math.max(0, torneo.getCuposOcupados() - 1));
			torneoRepository.save(torneo);
		}

		inscripcion.setEstado(estadoDestino);
		return ResponseEntity.ok(inscripcionRepository.save(inscripcion));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Cancelar/eliminar inscripción y liberar cupo si estaba aprobada")
	public ResponseEntity<?> eliminarInscripcion(@PathVariable("id") String id) {
		Optional<Inscripcion> inscOpt = inscripcionRepository.findById(id);
		if (inscOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Inscripción no encontrada con ID: " + id));
		}
		Inscripcion inscripcion = inscOpt.get();
		if ("APROBADA".equalsIgnoreCase(inscripcion.getEstado())) {
			torneoRepository.findById(inscripcion.getTorneoId()).ifPresent(torneo -> {
				torneo.setCuposOcupados(Math.max(0, torneo.getCuposOcupados() - 1));
				torneoRepository.save(torneo);
			});
		}
		inscripcionRepository.deleteById(id);
		return ResponseEntity.ok(Map.of("mensaje", "Inscripción eliminada correctamente"));
	}
}
