package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    BRAND_NOT_FOUND(404,"品牌没有查到"),
    CATEGORY_NOT_FOUND(404,"商品分类没有查找到"),
    BRAND_SAVE_ERROR(500,"品牌添加失败"),
    FILE_TYPE_MATCHING(400,"文件类型不匹配"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    SPEC_GROUP_NOT_FOUND(404,"规格组保参数存失败"),
    SPEC_PARAM_NOT_FOUND(404,"规格参数没有找到"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情没有找到"),
    GOODS_SAVE_ERROR(500,"商品保存失败"),
    GOODS_NOT_FOUND(404,"商品保存失败"),
    GOODS_SKU_NOT_FOUND(404,"商品库存保存失败"),
    SPEC_GROUP_SAVE_ERROR(404,"规格组保存失败"),
    SPEC_PRARM_UPDATE_ERROR(400,"规格参数修改失败"),
    SPEC_PRARM_DELETE_ERROR(400,"规格参数删除失败"),
    SPEC_GROUP_DELETE_ERROR(400,"规格组删除失败"),
    GOODS_STOCK_SAVE_ERROR(400,"商品库存保存失败"),
    DATA_TYPE_ERROR(400,"数据类型不正确"),
    VERIFICATION_CODE_ERROR(400,"验证码不正确"),
    USER_REGISTER_ERROR(400,"验证码不正确"),
    USERNAME_NOT_EXIST(400,"用户名不存在"),
    PASSWORD_WRONG(400,"密码错误"),
    TOKEN_ERROR(401,"token不正确"),
    UNAUTHORIZED (403,"未授权"),
    CART_NOT_FOUND(404,"购物车为空"),
    ORDER_SAVE_ERROR(500,"订单保存失败"),
    ORDERDETAIL_SAVE_ERROR(500,"订单详情保存失败"),
    STOCK_UPDATE_ERROR(500,"库存修改失败"),
    ORDER_STATUS_SAVE_ERROR(500,"订单状态保存失败"),
    ORDER_NOT_FOUND(404,"订单未找到"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情未找到"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态未找到"),
    WXPAY_OREDER_ERROR(404,"微信下单失败"),
    ORDER_STATUS_ERROR(500,"下单状态异常"),
    INVALID_SIGN_ERROR(500,"无效的签名"),
    INVALID_PARAM(400,"下单参数有误"),
    ;
    private Integer code;
    private String msg;

}
