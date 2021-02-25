package info.bitrich.xchangestream.binancemargin;

public enum BinanceSubscriptionType {
    DEPTH("depth"), TRADE("trade"), TICKER("ticker");

    private String type;

    BinanceSubscriptionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
