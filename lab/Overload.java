interface Customer {
    boolean isVIP();
}

class Merchant {
    public Number actionPrice(double price, Customer customer) {
        return 0;
    }
}

class NaiveMerchant extends Merchant {
    @Override
    public Double actionPrice(double price, Customer customer) {
        return 0.0;
    }
}
