package com.cydia.etcdkeeper.vo;

import lombok.Data;

@Data
public class StudentVo {

    private String name;

    private int age;

    private String address;

    public StudentVo(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }
}
