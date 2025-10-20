package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}