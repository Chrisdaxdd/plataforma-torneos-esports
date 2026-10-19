package com.esports.torneos.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esports.torneos.model.Torneo;

@Repository
public interface TorneoRepository extends MongoRepository<Torneo, String> {

	List<Torneo> findByEstadoIgnoreCase(String estado);

	List<Torneo> findByModalidadIgnoreCase(String modalidad);

	List<Torneo> findByOrganizadorNicknameIgnoreCase(String organizadorNickname);
}
