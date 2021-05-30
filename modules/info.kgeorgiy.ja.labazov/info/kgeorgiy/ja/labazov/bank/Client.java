package info.kgeorgiy.ja.labazov.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /** Utility class. */
    private Client() {}

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        final String accountId = args.length >= 1 ? args[0] : "geo";

        Account account = bank.getAccount(accountId);
        if (account == null) {
            System.out.println("Creating account");
            account = bank.createAccount(accountId);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + 100);
        System.out.println("Money: " + account.getAmount());

        System.out.println("Creating person");
        final boolean succ = bank.createPerson("alex", "t", 420);
        System.out.println("Creating account");
        bank.createAccount(420 + ":" + accountId);

        final Account localPerson1 = bank.getPerson(String.valueOf(420), false).getAccount(accountId);
        final Account remotePerson1 = bank.getPerson(String.valueOf(420), true).getAccount(accountId);

        System.out.println("Account id (local1): " + localPerson1.getId());
        System.out.println("Money (local1): " + localPerson1.getAmount());
        System.out.println("Adding money to local1");
        localPerson1.setAmount(localPerson1.getAmount() + 100);
        System.out.println("Money (local1): " + localPerson1.getAmount());

        System.out.println("Account id (remote1): " + remotePerson1.getId());
        System.out.println("Money (remote1): " + remotePerson1.getAmount());
        System.out.println("Adding money to remote1");
        remotePerson1.setAmount(remotePerson1.getAmount() + 100);
        System.out.println("Money (remote1): " + remotePerson1.getAmount());

        final Account localPerson2 = bank.getPerson(String.valueOf(420), false).getAccount(accountId);
        final Account remotePerson2 = bank.getPerson(String.valueOf(420), true).getAccount(accountId);

        System.out.println("Account id (local2): " + localPerson2.getId());
        System.out.println("Money (local2): " + localPerson2.getAmount());
        System.out.println("Adding money to local2");
        localPerson2.setAmount(localPerson2.getAmount() + 100);
        System.out.println("Money (local2): " + localPerson2.getAmount());

        System.out.println("Account id (remote2): " + remotePerson2.getId());
        System.out.println("Money (remote2): " + remotePerson2.getAmount());
        System.out.println("Adding money to remote2");
        remotePerson2.setAmount(remotePerson2.getAmount() + 100);
        System.out.println("Money (remote2): " + remotePerson2.getAmount());

        System.out.println("Account id (local1): " + localPerson1.getId());
        System.out.println("Money (local1): " + localPerson1.getAmount());
        System.out.println("Adding money to local1");
        localPerson1.setAmount(localPerson1.getAmount() + 100);
        System.out.println("Money (local1): " + localPerson1.getAmount());

        System.out.println("Account id (remote1): " + remotePerson1.getId());
        System.out.println("Money (remote1): " + remotePerson1.getAmount());
        System.out.println("Adding money to remote1");
        remotePerson1.setAmount(remotePerson1.getAmount() + 100);
        System.out.println("Money (remote1): " + remotePerson1.getAmount());
    }
}
