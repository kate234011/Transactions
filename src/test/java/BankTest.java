
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.NonExistentAccountException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import service.bank.Bank;
import service.runnable.TransactionTask;


@TestInstance(Lifecycle.PER_CLASS)
public class BankTest {

    private Bank bank;
    private ExecutorService executor;

    @BeforeAll
    public void setUp() {
        bank = new Bank();
        Map<String, Account> accountMap = generateAccounts();
        bank.setAccounts(accountMap);
    }

    @BeforeEach
    public void init() {
        executor = Executors.newFixedThreadPool(10);
    }


    @Test
    @DisplayName("Обработка транзакций, где есть фрод")
    public void transferAllWithFraud_test() {
        long expectedSaldo = bank.getSumAllAccounts();

        List<Account> accountList = bank.getAccounts().values().stream()
                .collect(Collectors.toList());

        for (int i = 0; i < accountList.size(); i++) {
            AtomicInteger idx = new AtomicInteger(i);
            if (idx.get() < 9) {
                TransactionTask task = new TransactionTask(bank,
                        accountList.get(idx.get()).getAccNumber(),
                        accountList.get(idx.incrementAndGet()).getAccNumber(),
                        71000);
                executor.execute(task);
            }
        }

        long actualSaldo = bank.getSumAllAccounts();

        assertEquals(expectedSaldo, actualSaldo);
        assertTrue(accountList.stream().noneMatch(Account::isBlocked));
    }

    @Test
    @DisplayName("Обработка транзакций, где нет фрода")
    public void transferAllWithoutFraud_test() {

        long expectedSaldo = bank.getSumAllAccounts();

        List<Account> accountList = bank.getAccounts().values().stream()
                .collect(Collectors.toList());

        for (int i = 0; i < accountList.size(); i++) {
            AtomicInteger idx = new AtomicInteger(i);
            if (idx.get() < 9) {
                TransactionTask task = new TransactionTask(bank,
                        accountList.get(idx.get()).getAccNumber(),
                        accountList.get(idx.incrementAndGet()).getAccNumber(),
                        10000);
                executor.execute(task);
            }
        }

        long actualSaldo = bank.getSumAllAccounts();

        assertEquals(expectedSaldo, actualSaldo);
    }

    @Test()
    @DisplayName("Обработка транзакций по счету, которого нет в банке")
    public void transferOnNonexistentAccount_test() {
        String sender = "408178100000256314";
        String recipient = "40817810000000234656";
        long amount = 1000;

        assertThrows(NonExistentAccountException.class,
                () -> bank.transfer(sender, recipient, amount));
    }

    private Map<String, Account> generateAccounts() {
        Map<String, Account> accounts = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            int tail = random.nextInt();
            String accNum = "4081781000" + (tail > 0 ? tail : tail * (-1));
            long value = 100000;

            Account account = new Account(accNum, value, false);

            accounts.put(accNum, account);
        }

        return accounts;
    }

    @AfterEach
    void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
        }
    }

    /*   //@Test // todo fix
    @DisplayName("Обработка транзакций, где есть и фрод-транзакции, и обычные транзакции")
    public void transferMixedTransactions_test() {

        long expectedSaldo = bank.getSumAllAccounts();

        List<Account> accountList = bank.getAccounts().values().stream()
                .collect(Collectors.toList());

        for (int i = 0; i < accountList.size(); i++) {
            AtomicInteger idx = new AtomicInteger(i);
            if (idx.get() < 9) {
                TransactionTask task = new TransactionTask(bank,
                        accountList.get(idx.get()).getAccNumber(),
                        accountList.get(idx.incrementAndGet()).getAccNumber(),
                        1000);
                executor.execute(task);
            }
        }

        long actualSaldo = bank.getSumAllAccounts();

        assertEquals(expectedSaldo, actualSaldo);
    }
    * */

}
