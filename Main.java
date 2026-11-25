public class Main {
    public static void main(String[] args) {
        Student s1 = new Student("202510845", "Anne", 20, 65, 72, 58);
        Student s2 = new Student("202511345", "Sara", 22, 40, 35, 48);

        s1.displayInformation();
        System.out.println("-------------------------");
        s2.displayInformation();
    }
}