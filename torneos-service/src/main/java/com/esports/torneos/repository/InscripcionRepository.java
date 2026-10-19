package com.esports.torneos.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esports.torneos.model.Inscripcion;

@Repository
public interface InscripcionRepository extends MongoRepository<Inscripcion, String> {

	List<Inscripcion> findByTorneoId(String torneoId);

	List<Inscripcion> findByTorneoIdAndEstadoIgnoreCase(String torneoId, String estado);

	boolean existsByTorneoIdAndNombreParticipanteIgnoreCase(String torneoId, String nombreParticipante);

	boolean existsByTorneoIdAndSolicitanteNicknameIgnoreCase(String torneoId, String solicitanteNickname);

	long countByTorneoIdAndEstadoIgnoreCase(String torneoId, String estado);
}
