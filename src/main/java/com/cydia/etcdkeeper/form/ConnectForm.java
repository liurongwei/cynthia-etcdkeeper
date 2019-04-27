package com.cydia.etcdkeeper.form;

import com.cydia.etcdkeeper.annotations.CookieProperty;
import lombok.Data;
import org.springframework.web.bind.annotation.CookieValue;

import javax.validation.constraints.NotBlank;

@Data
public class ConnectForm {

    private String host;

    private String uname;

    private String passwd;


}
