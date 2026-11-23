import java.util.Scanner;

// ===================== BankAccount =====================
class BankAccount {
    private static final int MAX_TRANSACTIONS = 100;

    private double balance;
    private String[] transactions;
    private int transactionCount;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
        this.transactions = new String[MAX_TRANSACTIONS];
        this.transactionCount = 0;
    }

    public double getBalance() {
        return balance;
    }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        recordTransaction("Deposit:    +" + String.format("%.2f", amount));
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        recordTransaction("Withdrawal: -" + String.format("%.2f", amount));
        return true;
    }

    // Prints the history using a for loop
    public void printTransactions() {
        if (transactionCount == 0) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.println("--- Transaction History ---");
        for (int i = 0; i < transactionCount; i++) {
            System.out.println((i + 1) + ". " + transactions[i]);
        }
    }

    private void recordTransaction(String entry) {
        if (transactionCount < MAX_TRANSACTIONS) {
            transactions[transactionCount] = entry;
            transactionCount++;
        } else {
            // Array full: drop the oldest entry and shift everything left
            for (int i = 1; i < MAX_TRANSACTIONS; i++) {
                transactions[i - 1] = transactions[i];
            }
            transactions[MAX_TRANSACTIONS - 1] = entry;
        }
    }
}

// ===================== Customer =====================
class Customer {
    private String name;
    private int pin;
    private BankAccount account;

    public Customer(String name, int pin, BankAccount account) {
        this.name = name;
        this.pin = pin;
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public BankAccount getAccount() {
        return account;
    }

    // The PIN is never exposed; callers can only check a guess
    public boolean authenticate(int enteredPin) {
        return this.pin == enteredPin;
    }
}

// ===================== Main =====================
public class Main {
    private static final int MAX_ATTEMPTS = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        BankAccount account = new BankAccount(1000.00);
        Customer customer = new Customer("Alex", 1234, account);

        // ---------- Authentication (do-while) ----------
        int attempts = 0;
        boolean loggedIn = false;

        do {
            System.out.print("Enter your PIN: ");
            int pin = readInt(scanner);
            attempts++;

            if (customer.authenticate(pin)) {
                loggedIn = true;
            } else {
                int left = MAX_ATTEMPTS - attempts;
                if (left > 0) {
                    System.out.println("Incorrect PIN. Attempts left: " + left);
                }
            }
        } while (!loggedIn && attempts < MAX_ATTEMPTS);

        if (!loggedIn) {
            System.out.println("Too many failed attempts. Card blocked. Goodbye.");
            scanner.close();
            return;
        }

        System.out.println("\nWelcome, " + customer.getName() + "!");
        BankAccount acc = customer.getAccount();

        // ---------- Main menu (while) ----------
        boolean running = true;
        while (running) {
            System.out.println("\n===== ATM MENU =====");
            System.out.println("1. Check balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. View transactions");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    System.out.printf("Your balance is: %.2f%n", acc.getBalance());
                    break;
                case 2:
                    System.out.print("Enter deposit amount: ");
                    double dep = readDouble(scanner);
                    if (acc.deposit(dep)) {
                        System.out.printf("Deposited %.2f. New balance: %.2f%n", dep, acc.getBalance());
                    } else {
                        System.out.println("Invalid amount.");
                    }
                    break;
                case 3:
                    System.out.print("Enter withdrawal amount: ");
                    double wd = readDouble(scanner);
                    if (acc.withdraw(wd)) {
                        System.out.printf("Withdrew %.2f. New balance: %.2f%n", wd, acc.getBalance());
                    } else {
                        System.out.println("Error: invalid amount or insufficient funds.");
                    }
                    break;
                case 4:
                    acc.printTransactions();
                    break;
                case 5:
                    running = false;
                    System.out.println("Thank you for using the ATM. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-5.");
            }
        }

        scanner.close();
    }

    // Safe input helpers so typing letters doesn't crash the program
    private static int readInt(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double readDouble(Scanner scanner) {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}