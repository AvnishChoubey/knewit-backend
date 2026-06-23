package com.knewit.backend.user.repository;

import com.knewit.backend.user.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findAllByUser_Id(Long userId);
    void deleteAllByUser_Id(Long userId);
}
