package com.citi.trade.model;

public enum TradeType {
    BUY("BUY"),
    SELL("SELL");

    private String tradeType;

    private TradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeType() {
        return this.tradeType;
    } 
}
