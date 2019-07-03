package com.leyou.order.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String name;//收货人姓名
    private String phone;//手机
    private String state;//省份
    private String city;//城市
    private String district;//区
    private String zipCode;//邮编
    private String address;//收获地址
    private Boolean isDefault;

}
