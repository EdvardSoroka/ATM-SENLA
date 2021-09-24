
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

public class User {

    private String firstName;
    private String lastName;
    private String uuid;
    private byte pinHash[];
    private ArrayList<Account> accounts;
    private AccountStatus status = AccountStatus.ACTIVE;
    private Date locDate;
    private int attemptsCount = 0;

    public User(String firstName, String lastName, String pin, Bank theBank){
        this.firstName = firstName;
        this.lastName = lastName;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            this.pinHash = messageDigest.digest(pin.getBytes());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("error, caught NoSuchAlgorithmException");
            e.printStackTrace();
            System.exit(1);
        }

        this.uuid = theBank.getNewUserUUID();
        this.accounts = new ArrayList<Account>();
        // log message
        System.out.printf("New user %s, %s with ID %s created, \n", lastName, firstName, this.uuid);
    }

    public void addAccount(Account anAcct){
        this.accounts.add(anAcct);
    }


    public boolean validatePin(String pin){
        if(isLocked()){
            throw new RuntimeException("Your account is locked. Please try later.");
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            if(MessageDigest.isEqual(messageDigest.digest(pin.getBytes()), this.pinHash)){
                attemptsCount = 0;
                return true;
            }else{
                this.attemptsCount++;
                if(attemptsCount >= 3){
                    status = AccountStatus.LOCKED;
                    locDate = new Date();
                    throw new RuntimeException("Your account is locked for 1 minute");
                }
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("error, caught NoSuchAlgorithmException");
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }

    public boolean isLocked(){
        if(AccountStatus.LOCKED.equals(status)){
            // блокируем аккаунт на одну минуту для удобства тестирования
            if(new Date().getTime() - locDate.getTime() < 60*1000){
                return true;
            }else{
                status = AccountStatus.ACTIVE;
                attemptsCount = 0;
                locDate = null;
            }
        }
        return false;
    }


    public String getFirstName() {
        return this.firstName;
    }

    public void printAccountsSummary(){
        System.out.printf("\n\n%s's accounts summary\n", this.firstName);
        for(int i = 0; i < this.accounts.size(); i++){
            System.out.printf(" %d) %s\n", i+1,
                    this.accounts.get(i).getSummaryLine());
        }
        System.out.println();

    }


    public int numAccounts() {
        return this.accounts.size();
    }

    public void printAcctTransHistory(int acctIdx) {
        this.accounts.get(acctIdx).printTransHistory();
    }

    public double getAcctBalance(int acctIdx) {
        return this.accounts.get(acctIdx).getBalance();
    }

    public String getAcctUUID(int acctIdx) {
        return this.accounts.get(acctIdx).getUUID();
    }

    public void addAcctTransaction(int acctIdx, double amount, String memo) {
        this.accounts.get(acctIdx).addTransaction(amount, memo);
    }

    public String getUUID() {
        return uuid;
    }
}
