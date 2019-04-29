package com.cydia.etcdkeeper.vo;

import com.cydia.etcdkeeper.enums.ResultCodes;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JsonResult<T> {

    private int code;

    private String message;

    private T data;

    public JsonResult success(){
        this.setCode(ResultCodes.Success.getCode());
        this.setMessage(ResultCodes.Success.getMessage());
        return this;
    }

    public JsonResult failed(){
        this.setCode(ResultCodes.Error.getCode());
        this.setMessage(ResultCodes.Error.getMessage());
        return this;
    }
}
