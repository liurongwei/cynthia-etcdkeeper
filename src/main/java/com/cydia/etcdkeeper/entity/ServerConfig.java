package com.cydia.etcdkeeper.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.context.annotation.Lazy;

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

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String endpoints;

    private boolean useTls;

    @Column(length = 255)
    private String keyFile;

    @Column(length = 255)
    private String caFile;

    @Column(length = 255)
    private String certFile;

    private boolean useAuth;

    @Column(length = 64)
    private String username;

    @Column(length = 32)
    private String password;

    @CreationTimestamp
    private Date createTime;

    @UpdateTimestamp
    private Date updateTime;
}
