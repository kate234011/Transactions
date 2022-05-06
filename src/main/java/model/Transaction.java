package model;

import lombok.Getter;

@Getter
public class Transaction {

    private final Account senderAccount;

    private final Account recipientAccount;

    private final long amount;


    public Transaction(Account senderAccount, Account recipientAccount, long amount) {
        this.senderAccount = senderAccount;
        this.recipientAccount = recipientAccount;
        this.amount = amount;
    }

}
