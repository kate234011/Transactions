package service.runnable;

import exception.NonExistentAccountException;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import service.bank.Bank;


@AllArgsConstructor
public class TransactionTask implements Runnable{

    public static final Logger log = Logger.getLogger(TransactionTask.class.getName());

    private Bank bank;

    private String senderAcc;

    private String recipientAcc;

    private long amount;


    @Override
    public void run() {
        try {
            bank.transfer(senderAcc, recipientAcc, amount);
        } catch (NonExistentAccountException e) {
           log.warning(e.getMessage());
        }
    }
}
