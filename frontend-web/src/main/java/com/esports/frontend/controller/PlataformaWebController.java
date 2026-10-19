package com.esports.frontend.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Controller
public class PlataformaWebController {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${esports.api.torneos-url:http://localhost:8081}")
	private String torneosApiUrl;

	@Value("${esports.api.matchmaking-url:http://localhost:8083}")
	private String matchmakingApiUrl;

	@jakarta.annotation.PostConstruct
	public void init() {
		if (torneosApiUrl != null && !torneosApiUrl.isBlank() && !torneosApiUrl.startsWith("http://") && !torneosApiUrl.startsWith("https://")) {
			torneosApiUrl = "https://" + torneosApiUrl;
		}
		if (matchmakingApiUrl != null && !matchmakingApiUrl.isBlank() && !matchmakingApiUrl.startsWith("http://") && !matchmakingApiUrl.startsWith("https://")) {
			matchmakingApiUrl = "https://" + matchmakingApiUrl;
		}
	}

	@GetMapping("/")
	public String mostrarDashboard(
			@RequestParam(name = "torneoId", required = false) String torneoId,
			@RequestParam(name = "msgExito", required = false) String msgExito,
			@RequestParam(name = "msgError", required = false) String msgError,
			HttpSession session,
			Model model) {

		if (msgExito != null && !msgExito.isBlank()) {
			model.addAttribute("mensajeExito", msgExito);
		}
		if (msgError != null && !msgError.isBlank()) {
			model.addAttribute("mensajeError", msgError);
		}

		List<Map<String, Object>> usuarios = obtenerListaSegura(torneosApiUrl + "/api/usuarios");
		List<Map<String, Object>> torneos = obtenerListaSegura(torneosApiUrl + "/api/torneos");

		String torneoSeleccionadoId = torneoId;
		if ((torneoSeleccionadoId == null || torneoSeleccionadoId.isBlank()) && !torneos.isEmpty()) {
			Object primerId = torneos.get(0).get("id");
			if (primerId != null) {
				torneoSeleccionadoId = primerId.toString();
			}
		}

		Map<String, Object> torneoActual = null;
		List<Map<String, Object>> inscripciones = Collections.emptyList();
		List<Map<String, Object>> enfrentamientos = Collections.emptyList();
		String campeonTorneo = null;

		if (torneoSeleccionadoId != null && !torneoSeleccionadoId.isBlank()) {
			for (Map<String, Object> t : torneos) {
				if (torneoSeleccionadoId.equals(String.valueOf(t.get("id")))) {
					torneoActual = t;
					break;
				}
			}
			inscripciones = obtenerListaSegura(
					torneosApiUrl + "/api/inscripciones?torneoId=" + torneoSeleccionadoId);
			enfrentamientos = obtenerListaSegura(
					matchmakingApiUrl + "/api/matchmaking/enfrentamientos?torneoId=" + torneoSeleccionadoId);

			for (Map<String, Object> match : enfrentamientos) {
				String nombreRonda = String.valueOf(match.getOrDefault("nombreRonda", ""));
				boolean validado = Boolean.TRUE.equals(match.get("resultadoValidado"));
				if ("GRAN FINAL".equalsIgnoreCase(nombreRonda) && validado && match.get("ganador") != null) {
					campeonTorneo = String.valueOf(match.get("ganador"));
				}
			}
		}

		model.addAttribute("usuarios", usuarios);
		model.addAttribute("torneos", torneos);
		model.addAttribute("torneoActual", torneoActual);
		model.addAttribute("torneoSeleccionadoId", torneoSeleccionadoId);
		model.addAttribute("inscripciones", inscripciones);
		model.addAttribute("enfrentamientos", enfrentamientos);
		model.addAttribute("campeonTorneo", campeonTorneo);
		model.addAttribute("torneosApiUrl", torneosApiUrl);
		model.addAttribute("matchmakingApiUrl", matchmakingApiUrl);

		return "index";
	}

	@PostMapping("/usuarios/registrar")
	public String registrarUsuario(
			@RequestParam(name = "nickname") String nickname,
			@RequestParam(name = "nombreCompleto") String nombreCompleto,
			@RequestParam(name = "email") String email,
			@RequestParam(name = "rol") String rol,
			@RequestParam(name = "equipoNombre", required = false) String equipoNombre,
			@RequestParam(name = "jugadoresConfirmadosCsv", required = false) String jugadoresConfirmadosCsv,
			RedirectAttributes redirectAttributes) {

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("nickname", nickname);
		payload.put("nombreCompleto", nombreCompleto);
		payload.put("email", email);
		payload.put("rol", rol);
		payload.put("equipoNombre", equipoNombre);
		if (jugadoresConfirmadosCsv != null && !jugadoresConfirmadosCsv.isBlank()) {
			List<String> plantilla = Arrays.stream(jugadoresConfirmadosCsv.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.collect(Collectors.toList());
			payload.put("jugadoresConfirmados", plantilla);
		}

		return enviarPostYRedirigir(torneosApiUrl + "/api/usuarios", payload,
				"Usuario '" + nickname + "' registrado exitosamente con rol " + rol,
				null);
	}

	@PostMapping("/torneos/crear")
	public String crearTorneo(
			@RequestParam(name = "nombre") String nombre,
			@RequestParam(name = "videojuego") String videojuego,
			@RequestParam(name = "modalidad") String modalidad,
			@RequestParam(name = "cupoMaximo") int cupoMaximo,
			@RequestParam(name = "reglas") String reglas,
			@RequestParam(name = "organizadorNickname") String organizadorNickname,
			@RequestParam(name = "fechaInicio", required = false) String fechaInicio,
			RedirectAttributes redirectAttributes) {

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("nombre", nombre);
		payload.put("videojuego", videojuego);
		payload.put("modalidad", modalidad);
		payload.put("formato", "ELIMINACION_DIRECTA");
		payload.put("cupoMaximo", cupoMaximo);
		payload.put("reglas", reglas);
		payload.put("organizadorNickname", organizadorNickname);
		payload.put("fechaInicio", fechaInicio);

		return enviarPostYRedirigir(torneosApiUrl + "/api/torneos", payload,
				"Torneo '" + nombre + "' creado exitosamente bajo modalidad " + modalidad,
				null);
	}

	@PostMapping("/inscripciones/registrar")
	public String registrarInscripcion(
			@RequestParam(name = "torneoId") String torneoId,
			@RequestParam(name = "nombreParticipante") String nombreParticipante,
			@RequestParam(name = "tipoParticipacion") String tipoParticipacion,
			@RequestParam(name = "solicitanteNickname") String solicitanteNickname,
			@RequestParam(name = "rolSolicitante") String rolSolicitante,
			@RequestParam(name = "plantillaCsv", required = false) String plantillaCsv,
			RedirectAttributes redirectAttributes) {

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("torneoId", torneoId);
		payload.put("nombreParticipante", nombreParticipante);
		payload.put("tipoParticipacion", tipoParticipacion);
		payload.put("solicitanteNickname", solicitanteNickname);
		payload.put("rolSolicitante", rolSolicitante);
		payload.put("estado", "APROBADA");

		if (plantillaCsv != null && !plantillaCsv.isBlank()) {
			List<String> plantilla = Arrays.stream(plantillaCsv.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.collect(Collectors.toList());
			payload.put("plantillaJugadores", plantilla);
		}

		return enviarPostYRedirigir(torneosApiUrl + "/api/inscripciones", payload,
				"Inscripción de '" + nombreParticipante + "' procesada y aprobada correctamente",
				torneoId);
	}

	@PostMapping("/matchmaking/generar")
	public String generarCuadro(
			@RequestParam(name = "torneoId") String torneoId,
			@RequestParam(name = "nombreTorneo") String nombreTorneo,
			@RequestParam(name = "arbitroNickname") String arbitroNickname,
			@RequestParam(name = "horarioProgramado", required = false) String horarioProgramado,
			RedirectAttributes redirectAttributes) {

		List<Map<String, Object>> inscripcionesAprobadas = obtenerListaSegura(
				torneosApiUrl + "/api/inscripciones?torneoId=" + torneoId + "&estado=APROBADA");

		List<String> participantes = new ArrayList<>();
		for (Map<String, Object> insc : inscripcionesAprobadas) {
			Object nombre = insc.get("nombreParticipante");
			if (nombre != null) {
				participantes.add(nombre.toString());
			}
		}

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("torneoId", torneoId);
		payload.put("nombreTorneo", nombreTorneo);
		payload.put("participantesAprobados", participantes);
		payload.put("arbitroNickname", arbitroNickname);
		payload.put("horarioProgramado", horarioProgramado);

		return enviarPostYRedirigir(matchmakingApiUrl + "/api/matchmaking/generar-cuadro", payload,
				"Cuadro de eliminación directa generado con " + participantes.size() + " participantes aprobados",
				torneoId);
	}

	@PostMapping("/matchmaking/validar/{matchId}")
	public String validarResultado(
			@PathVariable("matchId") String matchId,
			@RequestParam(name = "torneoId") String torneoId,
			@RequestParam(name = "arbitroNickname") String arbitroNickname,
			@RequestParam(name = "puntajeA") Integer puntajeA,
			@RequestParam(name = "puntajeB") Integer puntajeB,
			@RequestParam(name = "incidencias", required = false) String incidencias,
			@RequestParam(name = "validarOficialmente", defaultValue = "true") boolean validarOficialmente,
			RedirectAttributes redirectAttributes) {

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("arbitroNickname", arbitroNickname);
		payload.put("puntajeA", puntajeA);
		payload.put("puntajeB", puntajeB);
		payload.put("incidencias", incidencias);
		payload.put("validarOficialmente", validarOficialmente);

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
			restTemplate.exchange(
					matchmakingApiUrl + "/api/matchmaking/enfrentamientos/" + matchId + "/resultado",
					HttpMethod.PUT,
					entity,
					Map.class);
			String msg = validarOficialmente
					? "Resultado oficial validado por el árbitro '" + arbitroNickname + "'. El ganador avanza de ronda."
					: "Resultado preliminar registrado sin validación oficial (sin avance de ronda).";
			return construirRedirect(torneoId, msg, null);
		} catch (HttpStatusCodeException ex) {
			return construirRedirect(torneoId, null,
					"Regla de negocio rechazada (" + ex.getStatusCode() + "): " + ex.getResponseBodyAsString());
		} catch (Exception ex) {
			return construirRedirect(torneoId, null,
					"Error de comunicación con matchmaking-service: " + ex.getMessage());
		}
	}

	@ExceptionHandler(Exception.class)
	public String manejarExcepcionGeneral(Exception ex, HttpSession session, Model model) {
		return mostrarDashboard(null, null, "Aviso del sistema: " + ex.getMessage(), session, model);
	}

	private List<Map<String, Object>> obtenerListaSegura(String url) {
		try {
			ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
					url,
					HttpMethod.GET,
					null,
					new ParameterizedTypeReference<List<Map<String, Object>>>() {
					});
			return response.getBody() != null ? response.getBody() : Collections.emptyList();
		} catch (Exception ex) {
			return Collections.emptyList();
		}
	}

	private String enviarPostYRedirigir(String url, Map<String, Object> payload, String exitoMsg, String torneoId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
			restTemplate.postForEntity(url, request, Object.class);
			return construirRedirect(torneoId, exitoMsg, null);
		} catch (HttpStatusCodeException ex) {
			return construirRedirect(torneoId, null,
					"Validación de negocio (" + ex.getStatusCode() + "): " + ex.getResponseBodyAsString());
		} catch (Exception ex) {
			return construirRedirect(torneoId, null,
					"No fue posible conectar con el microservicio backend: " + ex.getMessage());
		}
	}

	private String construirRedirect(String torneoId, String msgExito, String msgError) {
		StringBuilder sb = new StringBuilder("redirect:/?");
		if (torneoId != null && !torneoId.isBlank()) {
			sb.append("torneoId=").append(UriUtils.encodeQueryParam(torneoId, StandardCharsets.UTF_8)).append("&");
		}
		if (msgExito != null && !msgExito.isBlank()) {
			sb.append("msgExito=").append(UriUtils.encodeQueryParam(msgExito, StandardCharsets.UTF_8));
		} else if (msgError != null && !msgError.isBlank()) {
			sb.append("msgError=").append(UriUtils.encodeQueryParam(msgError, StandardCharsets.UTF_8));
		}
		return sb.toString();
	}
}
