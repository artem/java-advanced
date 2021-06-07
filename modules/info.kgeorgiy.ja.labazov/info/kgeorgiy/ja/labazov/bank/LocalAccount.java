package info.kgeorgiy.ja.labazov.bank;

import java.io.Serializable;
import java.util.Objects;

public class LocalAccount implements Account, Serializable {
    private final String id;
    private int amount;

    public LocalAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    LocalAccount(final LocalAccount other) {
        this.id = other.id;
        amount = other.amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }

    @Override
    public synchronized void addAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount += amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalAccount that = (LocalAccount) o;
        return amount == that.amount && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount);
    }
}