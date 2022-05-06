package model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class Account implements Comparable<Account> {

    private String accNumber;

    private long money;

    private boolean isBlocked;


    @Override
    public int compareTo(Account o) {
        return this.hashCode() > o.hashCode() ? 1 : -1;
    }
}
