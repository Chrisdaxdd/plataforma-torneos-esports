package com.esports.matchmaking.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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

import com.esports.matchmaking.model.Enfrentamiento;
import com.esports.matchmaking.model.GeneracionCuadroRequest;
import com.esports.matchmaking.model.ValidacionResultadoRequest;
import com.esports.matchmaking.repository.EnfrentamientoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/matchmaking")
@CrossOrigin(origins = "*")
@Tag(name = "Emparejamiento y Resultados", description = "Gestión directa de cuadros de eliminación directa, llaves y validación oficial de resultados por árbitro")
public class EnfrentamientoController {

	private static final Set<Integer> CANTIDADES_VALIDAS = Set.of(2, 4, 8, 16, 32);

	private final EnfrentamientoRepository enfrentamientoRepository;

	public EnfrentamientoController(EnfrentamientoRepository enfrentamientoRepository) {
		this.enfrentamientoRepository = enfrentamientoRepository;
	}

	@GetMapping("/enfrentamientos")
	@Operation(summary = "Listar enfrentamientos de todos los torneos o filtrar por torneoId / árbitro")
	public ResponseEntity<List<Enfrentamiento>> listarEnfrentamientos(
			@RequestParam(name = "torneoId", required = false) String torneoId,
			@RequestParam(name = "arbitroNickname", required = false) String arbitroNickname) {
		if (torneoId != null && !torneoId.isBlank()) {
			return ResponseEntity.ok(
					enfrentamientoRepository.findByTorneoIdOrderByRondaAscNumeroLlaveAsc(torneoId.trim()));
		}
		if (arbitroNickname != null && !arbitroNickname.isBlank()) {
			return ResponseEntity.ok(
					enfrentamientoRepository.findByArbitroNicknameIgnoreCase(arbitroNickname.trim()));
		}
		return ResponseEntity.ok(enfrentamientoRepository.findAll());
	}

	@GetMapping("/enfrentamientos/{id}")
	@Operation(summary = "Consultar enfrentamiento por ID")
	public ResponseEntity<?> obtenerEnfrentamiento(@PathVariable("id") String id) {
		Optional<Enfrentamiento> matchOpt = enfrentamientoRepository.findById(id);
		if (matchOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Enfrentamiento no encontrado con ID: " + id));
		}
		return ResponseEntity.ok(matchOpt.get());
	}

	@PostMapping("/generar-cuadro")
	@Operation(summary = "Generar cuadro inicial de eliminación directa a partir de participantes aprobados (Rol Organizador)")
	public ResponseEntity<?> generarCuadroEliminacion(@Valid @RequestBody GeneracionCuadroRequest request) {
		String torneoId = request.getTorneoId().trim();
		if (enfrentamientoRepository.existsByTorneoId(torneoId)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Ya existe un cuadro de eliminación generado para este torneo"));
		}

		Set<String> participantesUnicos = new LinkedHashSet<>();
		for (String p : request.getParticipantesAprobados()) {
			if (p != null && !p.isBlank()) {
				if (!participantesUnicos.add(p.trim())) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Map.of("error", "Participante duplicado en la generación de llaves: " + p));
				}
			}
		}

		List<String> participantes = new ArrayList<>(participantesUnicos);
		if (!CANTIDADES_VALIDAS.contains(participantes.size())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Para eliminación directa la cantidad de inscritos aprobados debe ser potencia de 2 (2, 4, 8, 16 o 32). Actual: "
							+ participantes.size()));
		}

		String arbitro = request.getArbitroNickname().trim();
		for (String p : participantes) {
			if (p.equalsIgnoreCase(arbitro)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Conflicto de interés: el árbitro '" + arbitro
								+ "' no puede formar parte de los participantes enfrentados"));
			}
		}

		int totalLlavesRonda1 = participantes.size() / 2;
		String nombreRondaInicial = calcularNombreRonda(totalLlavesRonda1);
		List<Enfrentamiento> llavesCreadas = new ArrayList<>();

		for (int i = 0; i < totalLlavesRonda1; i++) {
			Enfrentamiento match = new Enfrentamiento();
			match.setTorneoId(torneoId);
			match.setNombreTorneo(request.getNombreTorneo() != null ? request.getNombreTorneo() : "Torneo " + torneoId);
			match.setRonda(1);
			match.setNombreRonda(nombreRondaInicial);
			match.setNumeroLlave(i + 1);
			match.setParticipanteA(participantes.get(i * 2));
			match.setParticipanteB(participantes.get(i * 2 + 1));
			match.setArbitroNickname(arbitro);
			match.setHorarioProgramado(request.getHorarioProgramado() != null && !request.getHorarioProgramado().isBlank()
					? request.getHorarioProgramado()
					: "Por confirmar");
			match.setResultadoValidado(false);
			match.setEstado("PROGRAMADO");
			llavesCreadas.add(enfrentamientoRepository.save(match));
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(llavesCreadas);
	}

	@PostMapping("/enfrentamientos")
	@Operation(summary = "Crear enfrentamiento manual validando conflicto de interés del árbitro")
	public ResponseEntity<?> crearEnfrentamientoManual(@Valid @RequestBody Enfrentamiento enfrentamiento) {
		if (enfrentamiento.getParticipanteA().trim().equalsIgnoreCase(enfrentamiento.getParticipanteB().trim())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Un participante no puede enfrentarse a sí mismo"));
		}

		if (tieneConflictoArbitro(enfrentamiento, enfrentamiento.getArbitroNickname())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Regla de imparcialidad: no se permite asignar como árbitro a un integrante de los participantes enfrentados"));
		}

		enfrentamiento.setResultadoValidado(false);
		enfrentamiento.setEstado("PROGRAMADO");
		return ResponseEntity.status(HttpStatus.CREATED).body(enfrentamientoRepository.save(enfrentamiento));
	}

	@PutMapping("/enfrentamientos/{id}/resultado")
	@Operation(summary = "Registrar y validar resultado oficial por Árbitro (solo un resultado validado produce avance de ronda)")
	public ResponseEntity<?> registrarYValidarResultado(
			@PathVariable("id") String id,
			@Valid @RequestBody ValidacionResultadoRequest request) {
		Optional<Enfrentamiento> matchOpt = enfrentamientoRepository.findById(id);
		if (matchOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Enfrentamiento no encontrado con ID: " + id));
		}

		Enfrentamiento match = matchOpt.get();

		if ("POR_DEFINIR".equalsIgnoreCase(match.getParticipanteA())
				|| "POR_DEFINIR".equalsIgnoreCase(match.getParticipanteB())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Aún no están definidos ambos participantes de esta llave"));
		}

		if (tieneConflictoArbitro(match, request.getArbitroNickname())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "Conflicto de interés: el árbitro no puede ser integrante de los participantes enfrentados"));
		}

		if (!match.getArbitroNickname().equalsIgnoreCase(request.getArbitroNickname().trim())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "Solo el árbitro asignado (" + match.getArbitroNickname()
							+ ") puede registrar y validar el resultado oficial de esta partida"));
		}

		if (request.getPuntajeA().equals(request.getPuntajeB())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "En eliminación directa no se permiten empates; debe existir un ganador definitivo"));
		}

		match.setPuntajeA(request.getPuntajeA());
		match.setPuntajeB(request.getPuntajeB());
		match.setIncidencias(request.getIncidencias());
		String ganador = request.getPuntajeA() > request.getPuntajeB()
				? match.getParticipanteA()
				: match.getParticipanteB();
		match.setGanador(ganador);

		if (request.isValidarOficialmente()) {
			match.setResultadoValidado(true);
			match.setEstado("VALIDADO_OFICIAL");
			match.setFechaValidacion(LocalDateTime.now());
			enfrentamientoRepository.save(match);

			avanzarGanadorSiguienteRonda(match);
		} else {
			match.setResultadoValidado(false);
			match.setEstado("RESULTADO_REGISTRADO");
			enfrentamientoRepository.save(match);
		}

		return ResponseEntity.ok(match);
	}

	@DeleteMapping("/torneo/{torneoId}")
	@Operation(summary = "Reiniciar/eliminar el cuadro de enfrentamientos de un torneo")
	public ResponseEntity<?> eliminarCuadroTorneo(@PathVariable("torneoId") String torneoId) {
		List<Enfrentamiento> matches = enfrentamientoRepository.findByTorneoIdOrderByRondaAscNumeroLlaveAsc(torneoId);
		if (!matches.isEmpty()) {
			enfrentamientoRepository.deleteAll(matches);
		}
		return ResponseEntity.ok(Map.of("mensaje", "Cuadro de eliminación reiniciado correctamente"));
	}

	private boolean tieneConflictoArbitro(Enfrentamiento match, String arbitroNickname) {
		if (arbitroNickname == null || arbitroNickname.isBlank()) {
			return false;
		}
		String arb = arbitroNickname.trim();
		if (arb.equalsIgnoreCase(match.getParticipanteA()) || arb.equalsIgnoreCase(match.getParticipanteB())) {
			return true;
		}
		if (match.getIntegrantesParticipanteA() != null) {
			for (String integrante : match.getIntegrantesParticipanteA()) {
				if (arb.equalsIgnoreCase(integrante)) {
					return true;
				}
			}
		}
		if (match.getIntegrantesParticipanteB() != null) {
			for (String integrante : match.getIntegrantesParticipanteB()) {
				if (arb.equalsIgnoreCase(integrante)) {
					return true;
				}
			}
		}
		return false;
	}

	private void avanzarGanadorSiguienteRonda(Enfrentamiento matchActual) {
		List<Enfrentamiento> llavesRondaActual = enfrentamientoRepository
				.findByTorneoIdAndRondaOrderByNumeroLlaveAsc(matchActual.getTorneoId(), matchActual.getRonda());

		if (llavesRondaActual.size() <= 1 && "GRAN FINAL".equalsIgnoreCase(matchActual.getNombreRonda())) {
			return;
		}
		if (matchActual.getRonda() == 1 && llavesRondaActual.size() == 1) {
			return;
		}

		int siguienteRonda = matchActual.getRonda() + 1;
		int siguienteNumeroLlave = (matchActual.getNumeroLlave() + 1) / 2;
		boolean esLadoA = (matchActual.getNumeroLlave() % 2 != 0);

		int totalLlavesSiguienteRonda = Math.max(1, llavesRondaActual.size() / 2);
		String nombreSiguienteRonda = calcularNombreRonda(totalLlavesSiguienteRonda);

		Optional<Enfrentamiento> siguienteOpt = enfrentamientoRepository
				.findByTorneoIdAndRondaAndNumeroLlave(matchActual.getTorneoId(), siguienteRonda, siguienteNumeroLlave);

		Enfrentamiento siguienteMatch;
		if (siguienteOpt.isPresent()) {
			siguienteMatch = siguienteOpt.get();
		} else {
			siguienteMatch = new Enfrentamiento();
			siguienteMatch.setTorneoId(matchActual.getTorneoId());
			siguienteMatch.setNombreTorneo(matchActual.getNombreTorneo());
			siguienteMatch.setRonda(siguienteRonda);
			siguienteMatch.setNombreRonda(nombreSiguienteRonda);
			siguienteMatch.setNumeroLlave(siguienteNumeroLlave);
			siguienteMatch.setParticipanteA("POR_DEFINIR");
			siguienteMatch.setParticipanteB("POR_DEFINIR");
			siguienteMatch.setArbitroNickname(matchActual.getArbitroNickname());
			siguienteMatch.setHorarioProgramado("Siguiente ronda - Por programar");
			siguienteMatch.setResultadoValidado(false);
		}

		if (esLadoA) {
			siguienteMatch.setParticipanteA(matchActual.getGanador());
		} else {
			siguienteMatch.setParticipanteB(matchActual.getGanador());
		}

		if (!"POR_DEFINIR".equalsIgnoreCase(siguienteMatch.getParticipanteA())
				&& !"POR_DEFINIR".equalsIgnoreCase(siguienteMatch.getParticipanteB())) {
			siguienteMatch.setEstado("PROGRAMADO");
		} else {
			siguienteMatch.setEstado("PENDIENTE_RIVAL");
		}

		enfrentamientoRepository.save(siguienteMatch);
	}

	private String calcularNombreRonda(int cantidadLlavesEnRonda) {
		return switch (cantidadLlavesEnRonda) {
			case 1 -> "GRAN FINAL";
			case 2 -> "SEMIFINAL";
			case 4 -> "CUARTOS DE FINAL";
			case 8 -> "OCTAVOS DE FINAL";
			default -> "RONDA DE " + (cantidadLlavesEnRonda * 2);
		};
	}
}
