interface Customer {
    boolean isVIP();
}

class VIP implements Customer {

    @Override
    public boolean isVIP() {
        return true;
    }
}

class Merchant<T extends Customer> {
    public double actionPrice(double price, T customer) {
        return 0.0;
    }
}

class VIPOnlyMerchant extends Merchant<VIP> {
    @Override
    public double actionPrice(double price, VIP customer) {
        return 0.0;
    }
}
