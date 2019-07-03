package com.leyou.order.enums;

public enum OrderStatusEnum {

    UNPAY(1,"未付款"),
    PAY_NOT_DELIVERD(2,"已付款，未发货"),
    UNCONFIRM(3,"已发货,为确认"),
    SUCCESS(4,"交易成功"),
    CLOSE(5,"交易关闭"),
    RATED(6,"已评价"),
    ;
    private int code;
    private String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int value(){
        return this.code;
    }
}
