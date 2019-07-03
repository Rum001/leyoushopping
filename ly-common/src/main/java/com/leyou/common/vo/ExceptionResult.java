package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ExceptionResult {

    private String message;
    private Integer status;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum exceptionEnum) {
       this.status= exceptionEnum.getCode();
       this.message= exceptionEnum.getMsg();
       this.timestamp=System.currentTimeMillis();
    }
}
