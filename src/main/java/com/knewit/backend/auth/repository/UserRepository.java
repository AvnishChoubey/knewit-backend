package com.knewit.backend.auth.repository;

import com.knewit.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
