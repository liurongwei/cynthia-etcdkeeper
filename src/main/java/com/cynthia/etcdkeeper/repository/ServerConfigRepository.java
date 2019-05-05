package com.cynthia.etcdkeeper.repository;

import com.cynthia.etcdkeeper.entity.ServerConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerConfigRepository extends JpaRepository<ServerConfig, Integer> {
}
