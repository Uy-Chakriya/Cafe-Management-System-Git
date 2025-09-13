package com.example.cafeshopmanagement.Model;

public class Receipt {
    private Integer id;
    private String customer_id;
    private Double total;
    private String date;
    private String em_username;

    public Receipt(Integer id, String customerId, Double total, String date, String emUsername) {
        this.id = id;
        this.customer_id = customerId;
        this.total = total;
        this.date = date;
        this.em_username = emUsername;
    }
    public Integer getId() {
        return id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public Double getTotal() {
        return total;
    }

    public String getDate() {
        return date;
    }

    public String getEm_username() {
        return em_username;
    }
}