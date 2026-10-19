package com.esports.torneos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esports.torneos.model.Usuario;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

	Optional<Usuario> findByNicknameIgnoreCase(String nickname);

	Optional<Usuario> findByEmailIgnoreCase(String email);

	List<Usuario> findByRolIgnoreCase(String rol);

	boolean existsByNicknameIgnoreCase(String nickname);

	boolean existsByEmailIgnoreCase(String email);
}
