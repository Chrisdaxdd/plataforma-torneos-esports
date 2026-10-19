package com.esports.matchmaking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esports.matchmaking.model.Enfrentamiento;

@Repository
public interface EnfrentamientoRepository extends MongoRepository<Enfrentamiento, String> {

	List<Enfrentamiento> findByTorneoIdOrderByRondaAscNumeroLlaveAsc(String torneoId);

	List<Enfrentamiento> findByTorneoIdAndRondaOrderByNumeroLlaveAsc(String torneoId, int ronda);

	Optional<Enfrentamiento> findByTorneoIdAndRondaAndNumeroLlave(String torneoId, int ronda, int numeroLlave);

	List<Enfrentamiento> findByArbitroNicknameIgnoreCase(String arbitroNickname);

	boolean existsByTorneoId(String torneoId);
}
