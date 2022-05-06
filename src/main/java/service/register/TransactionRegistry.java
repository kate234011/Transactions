package service.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import model.Transaction;

public class TransactionRegistry {

    @Getter
    private List<Transaction> transactions;

    public TransactionRegistry(){
        this.transactions =  Collections.synchronizedList(new ArrayList<>());
    }

    public void registerTransaction(Transaction transaction){
        transactions.add(transaction);
    }

}
