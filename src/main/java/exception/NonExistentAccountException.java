package exception;

public class NonExistentAccountException extends Exception {

    public NonExistentAccountException(String accNum) {
        super("Указанного счета " + accNum + "не существует! Проводка отклонена");
    }

}