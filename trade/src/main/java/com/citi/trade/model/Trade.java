package com.citi.trade.model;

import java.util.Date;


//@Document
public class Trade {

//    @Id
//    private String _id;
    private Date created = new Date(System.currentTimeMillis());
    private TradeState state = TradeState.CREATED;
    private TradeType type = TradeType.BUY;
    private String ticker;
    private int quantity;
    private double price;


    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public TradeState getState() {
        return state;
    }

    public void setState(TradeState state) {
        this.state = state;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public TradeType getType() {
        return type;
    }

    public void setType(TradeType type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
