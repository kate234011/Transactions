package service.bank;

import static configarutaion.Configuration.FRAUD_PERCENT_AMOUNT;
import static configarutaion.Configuration.FRAUD_TRANSACTION_AMOUNT;

import exception.NonExistentAccountException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import model.Account;
import model.ErrorCode;
import model.Transaction;
import model.ValidateInfo;
import service.register.TransactionRegistry;

public class Bank {

    public static final Logger log = Logger.getLogger(Bank.class.getName());

    @Setter
    @Getter
    private Map<String, Account> accounts;
    private final TransactionRegistry transactionRegistry;
    private final Validator validator;

    public Bank() {
        this.accounts = Collections.synchronizedMap(new HashMap<>());//new ConcurrentHashMap<>();
        this.transactionRegistry = new TransactionRegistry();
        this.validator = new Validator();
    }

    /**
     * TODO: реализовать метод. Метод переводит деньги между счетами. Если сумма транзакции > 50000,
     * то после совершения транзакции, она отправляется на проверку Службе Безопасности – вызывается
     * метод isFraud. Если возвращается true, то делается блокировка счетов (как – на ваше
     * усмотрение)
     */
    public void transfer(String fromAccountNum, String toAccountNum, long amount)
            throws NonExistentAccountException {
        Account senderAcc = accounts.get(fromAccountNum);
        Account recipientAcc = accounts.get(toAccountNum);

        if (senderAcc == null) {
            throw new NonExistentAccountException(fromAccountNum);
        } else if (recipientAcc == null) {
            throw new NonExistentAccountException(toAccountNum);
        }

        Account lowSyncAccount = senderAcc.compareTo(recipientAcc) > 1 ? senderAcc : recipientAcc;
        Account topSyncAccount = lowSyncAccount.equals(senderAcc) ? recipientAcc : senderAcc;
        log.info("  " +topSyncAccount.isBlocked());
        log.info("  " +lowSyncAccount.isBlocked());

        Transaction transaction = new Transaction(senderAcc, recipientAcc, amount);
        transactionRegistry.registerTransaction(transaction);
        ValidateInfo transactionInfo = validator.validateTransaction(transaction);

        synchronized (lowSyncAccount) {
            synchronized (topSyncAccount) {

                if (transactionInfo.isValidate()) {
                    senderAcc.setMoney(senderAcc.getMoney() - amount);
                    recipientAcc.setMoney(recipientAcc.getMoney() + amount);
                } else {
                    log.warning("  1111" +transaction.getRecipientAccount().isBlocked());
                    if (transactionInfo.getErrorCode().equals(ErrorCode.FRAUD)) {
                        senderAcc.setBlocked(true);
                        recipientAcc.setBlocked(true);
                    }
                }
            }
        }
    }

    public long getBalance(String accountNum) {
        return accounts.get(accountNum).getMoney();
    }

    public long getSumAllAccounts() {
        return accounts.values().stream().mapToLong(account -> account.getMoney()).sum();
    }


    // todo вынеси в отдельный класс, подумать насчет TransactionRegistry
    class Validator {

        synchronized ValidateInfo validateTransaction(Transaction transaction) {

            Account sender = transaction.getSenderAccount();
            Account recipient = transaction.getRecipientAccount();

            ValidateInfo validateInfo;

            validateInfo = checkOverdraft(sender, transaction.getAmount());

            if (validateInfo.getErrorMessage() != null) {
                return validateInfo;
            }

            validateInfo = checkBlockedAccounts(sender, recipient);

            if (validateInfo.getErrorMessage() != null) {
                return validateInfo;
            }

            validateInfo = checkFraudTransaction(transaction.getAmount());

            if (validateInfo.getErrorMessage() != null) {
                return validateInfo;
            }

            validateInfo.setValidate(true);

            return validateInfo;
        }


        ValidateInfo checkOverdraft(Account sender, long amount) {
            if (sender.getMoney() < amount) {
                return new ValidateInfo(ErrorCode.OVERDRAFT, "На счете " + sender.getAccNumber()
                        + " недостаточно денег для перевода на сумму "
                        + amount, false);
            }
            return new ValidateInfo();
        }

        ValidateInfo checkBlockedAccounts(Account sender, Account recipient) {
            if (sender.isBlocked() || recipient.isBlocked()) {
                log.info("BLOCKED_ACCOUNT: " + Thread.currentThread().getName());
                return new ValidateInfo(ErrorCode.BLOCKED_ACCOUNT,
                        "Счет " + (sender.isBlocked() ? sender.getAccNumber()
                                : recipient.getAccNumber()) + " заблокирован!", false);
            }

            return new ValidateInfo();
        }

        ValidateInfo checkFraudTransaction(long amount) {
            if (amount > FRAUD_TRANSACTION_AMOUNT) {
                if (isFraud()) {
                    return new ValidateInfo(ErrorCode.FRAUD,
                            "Транзакция признана подозрительной. Оба счета будут заблокированы!",
                            false);
                }
            }
            return new ValidateInfo();
        }

         synchronized boolean isFraud() {
            List<Transaction> transactions = transactionRegistry.getTransactions();
            long fraudTransactionsCnt = transactions.stream()
                    .filter(transaction -> transaction.getAmount() > FRAUD_TRANSACTION_AMOUNT)
                    .count();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("Thread: " + Thread.currentThread().getName());
            return fraudTransactionsCnt > 0;//(fraudTransactionsCnt / transactions.size()) > FRAUD_PERCENT_AMOUNT;
        }
    }
}
