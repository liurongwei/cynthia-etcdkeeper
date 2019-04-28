package com.cydia.etcdkeeper.repository;

import com.cydia.etcdkeeper.entity.ServerConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerConfigRepository extends JpaRepository<ServerConfig, Integer> {
}
