package com.cynthia.etcdkeeper.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "server_config")
@Data
@Proxy(lazy = false)
public class ServerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 32)
    private String name = "";

    @Column(length = 255, nullable = false)
    private String title = "";

    @Column(length = 255, nullable = false)
    private String endpoints = "";

    @Column(length = 4,nullable = false)
    private String apiVersion = "2";

    @Column(nullable = false)
    private boolean useTls = false;

    @Column(length = 255)
    private String keyFile;

    @Column(length = 255)
    private String caFile;

    @Column(length = 255)
    private String certFile;

    @Column(nullable = false)
    private boolean useAuth = false;

    @Column(length = 64)
    private String username;

    @Column(length = 32)
    private String password;

    @Column(columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    @CreationTimestamp
    private Date createTime;

    @Column(columnDefinition = "timestamp default current_timestamp")
    @UpdateTimestamp
    private Date updateTime;
}
