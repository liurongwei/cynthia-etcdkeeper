package com.cydia.etcdkeeper.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "server_config")
@Data
public class ServerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

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
