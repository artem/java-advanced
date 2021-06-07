package info.kgeorgiy.ja.labazov.bank;

import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

@DisplayName("Bank unit tests")
public class JUnitBankTests {
    static final int PORT = 8888;
    static final String DEF_ACC_ID = "geo";
    static final String DEF_FIRST_NAME = "TIM";
    static final String DEF_LAST_NAME = "APPLE";
    static final long DEF_PASS_ID = 177013;
    static final String DEF_PASS_ID_STR = String.valueOf(DEF_PASS_ID);
    Bank bank;

    @BeforeAll
    static void beforeAll() throws RemoteException {
        try {
            LocateRegistry.createRegistry(PORT);
        } catch (final ExportException ignored) {
        }
    }

    static void getCheckDefAccount(final Account acc) throws RemoteException {
        Assertions.assertNotNull(acc);
        Assertions.assertEquals(DEF_ACC_ID, acc.getId());
    }

    static void checkDefPerson(final Person person) throws RemoteException {
        Assertions.assertNotNull(person);
        Assertions.assertEquals(DEF_FIRST_NAME, person.getFirstName());
        Assertions.assertEquals(DEF_LAST_NAME, person.getLastName());
        Assertions.assertEquals(DEF_PASS_ID, person.getPassportId());
    }

    @BeforeEach
    void beforeEach() throws RemoteException, MalformedURLException {
        bank = new RemoteBank(PORT);
        UnicastRemoteObject.exportObject(bank, PORT);
        Naming.rebind("//localhost:" + PORT + "/bank", bank);
    }

    @AfterEach
    void afterEach() {
        bank = null;
    }

    Account createAccount(final String id) throws RemoteException {
        final Account acc = bank.createAccount(id);
        Assertions.assertNotNull(acc);
        return acc;
    }

    Account getAccount(final String id) throws RemoteException {
        final Account acc = bank.getAccount(id);
        Assertions.assertNotNull(acc);
        return acc;
    }

    Person getPerson(final String id, final boolean remote) throws RemoteException {
        final Person person = bank.getPerson(id, remote);
        Assertions.assertNotNull(person);
        return person;
    }

    @Test
    public void testDefAccount() throws RemoteException {
        final Account acc = createAccount(DEF_ACC_ID);
        getCheckDefAccount(acc);
    }

    @Test
    public void testDefPerson() throws RemoteException {
        Assertions.assertTrue(bank.createPerson(DEF_FIRST_NAME, DEF_LAST_NAME, DEF_PASS_ID));
        checkDefPerson(bank.getPerson(DEF_PASS_ID_STR, false));
        checkDefPerson(bank.getPerson(DEF_PASS_ID_STR, true));
    }

    @Test
    public void testAccounts() throws RemoteException {
        final Account acc1 = createAccount(DEF_ACC_ID);
        final Account acc2 = createAccount(DEF_ACC_ID + 2);
        Assertions.assertNotEquals(acc2, acc1);

        acc2.addAmount(123);
        Assertions.assertNotEquals(acc2, acc1);

        acc1.addAmount(-123);
        Assertions.assertNotEquals(acc2, acc1);

        final Account acc3 = createAccount(DEF_ACC_ID);
        Assertions.assertEquals(acc1, acc3);
        acc3.addAmount(432);
        Assertions.assertEquals(acc1, acc3);
    }

    @Test
    public void testRemotePersons() throws RemoteException {
        bank.createPerson(DEF_FIRST_NAME, DEF_LAST_NAME, DEF_PASS_ID);
        //bank.createPerson(DEF_FIRST_NAME, DEF_LAST_NAME, DEF_PASS_ID*10);
        final Person person1 = getPerson(DEF_PASS_ID_STR, true);
        final Person person2 = getPerson(DEF_PASS_ID_STR, true);
        Assertions.assertEquals(person2, person1);

        createAccount(DEF_PASS_ID_STR + ":" + DEF_ACC_ID);
        final Account acc = getAccount(DEF_PASS_ID_STR + ":" + DEF_ACC_ID);
        Assertions.assertNotNull(person1.getAccount(DEF_ACC_ID));
        Assertions.assertNotNull(person2.getAccount(DEF_ACC_ID));
        Assertions.assertEquals(person2, person1);

        acc.addAmount(912);
        Assertions.assertEquals(person1.getAccount(DEF_ACC_ID).getAmount(), 912);
        Assertions.assertEquals(person2, person1);
    }

    @Test
    public void testLocalPersons() throws RemoteException {
        bank.createPerson(DEF_FIRST_NAME, DEF_LAST_NAME, DEF_PASS_ID);

        final Person person1 = getPerson(DEF_PASS_ID_STR, false);
        final Person person2 = getPerson(DEF_PASS_ID_STR, false);
        Assertions.assertEquals(person2, person1);

        createAccount(DEF_PASS_ID_STR + ":" + DEF_ACC_ID);
        final Account acc = getAccount(DEF_PASS_ID_STR + ":" + DEF_ACC_ID);
        final Person person3 = getPerson(DEF_PASS_ID_STR, false);
        Assertions.assertNull(person1.getAccount(DEF_ACC_ID));
        Assertions.assertNull(person2.getAccount(DEF_ACC_ID));
        Assertions.assertEquals(acc.getId(), person3.getAccount(DEF_ACC_ID).getId());
        Assertions.assertEquals(person2, person1);
        Assertions.assertNotEquals(person2, person3);

        acc.addAmount(912);
        Assertions.assertNull(person1.getAccount(DEF_ACC_ID));
        Assertions.assertNull(person2.getAccount(DEF_ACC_ID));
        Assertions.assertNotEquals(acc, person3.getAccount(DEF_ACC_ID));
        Assertions.assertEquals(person2, person1);
        Assertions.assertNotEquals(person2, person3);
    }

    private void parallelLaunch(final IntConsumer fun) throws InterruptedException {
        final int threads = 8;
        final int requests = 8;

        final ExecutorService pool = Executors.newFixedThreadPool(threads);

        IntStream.range(0, threads).forEach(i ->
                IntStream.range(i * 8, requests + i * 8).forEach(j ->
                        pool.submit(() -> {
                            fun.accept(j);
                        })));

        pool.awaitTermination(threads * requests * 50, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testConcurrentPersons() throws InterruptedException, RemoteException {
        parallelLaunch(j -> {
            try {
                bank.createPerson(DEF_FIRST_NAME + j, DEF_LAST_NAME + j, j);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        for (int i = 0; i < 64; i++) {
            final Person person = getPerson(String.valueOf(i), false);
            Assertions.assertEquals(DEF_FIRST_NAME + i, person.getFirstName());
            Assertions.assertEquals(DEF_LAST_NAME + i, person.getLastName());
            Assertions.assertEquals(i, person.getPassportId());
        }
    }

    @Test
    public void testConcurrentAmount() throws RemoteException, InterruptedException {
        final int DELTA = 3;
        Assertions.assertTrue(bank.createPerson(DEF_FIRST_NAME, DEF_LAST_NAME, DEF_PASS_ID));

        final Account account = createAccount(DEF_PASS_ID_STR + ":" + DEF_ACC_ID);
        parallelLaunch(j -> {
            try {
                account.addAmount(DELTA);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        Assertions.assertEquals(DELTA * 8 * 8, account.getAmount());
    }

    @Test
    public void testConcurrentAccounts() throws InterruptedException, RemoteException {
        parallelLaunch(j -> {
            try {
                bank.createAccount(DEF_ACC_ID + j);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        for (int i = 0; i < 64; i++) {
            Assertions.assertEquals(DEF_ACC_ID + i, getAccount(DEF_ACC_ID + i).getId());
        }
    }
}
