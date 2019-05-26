package com.one.learn.spring.springevents.generic;

/**
 * 订单
 *
 * @author One
 * @date 2019/05/26
 */
public class Order {
    private Integer orderNo;
    private String value;

    public Order(Integer orderNo, String value) {
        this.orderNo = orderNo;
        this.value = value;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNo=" + orderNo +
                ", value='" + value + '\'' +
                '}';
    }
}
