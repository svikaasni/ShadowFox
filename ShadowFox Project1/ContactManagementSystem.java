import java.util.ArrayList;
import java.util.Scanner;

class Contact {
    String name;
    String phone;
    String email;

    Contact(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    void display() {
        System.out.println("Name  : " + name);
        System.out.println("Phone : " + phone);
        System.out.println("Email : " + email);
        System.out.println("----------------------------");
    }
}

public class ContactManagementSystem {

    static ArrayList<Contact> contacts = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        int choice;

        do {
            System.out.println("\n===== CONTACT MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Contact");
            System.out.println("2. View Contacts");
            System.out.println("3. Update Contact");
            System.out.println("4. Delete Contact");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addContact();
                    break;

                case 2:
                    viewContacts();
                    break;

                case 3:
                    updateContact();
                    break;

                case 4:
                    deleteContact();
                    break;

                case 5:
                    System.out.println("Thank you for using Contact Management System!");
                    break;

                default:
                    System.out.println("Invalid Choice!");
            }

        } while (choice != 5);
    }

    // Add Contact
    static void addContact() {
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        contacts.add(new Contact(name, phone, email));

        System.out.println("Contact Added Successfully!");
    }

    // View Contacts
    static void viewContacts() {

        if (contacts.isEmpty()) {
            System.out.println("No Contacts Available.");
            return;
        }

        System.out.println("\n----- Contact List -----");

        for (int i = 0; i < contacts.size(); i++) {
            System.out.println("Contact ID: " + (i + 1));
            contacts.get(i).display();
        }
    }

    // Update Contact
    static void updateContact() {

        if (contacts.isEmpty()) {
            System.out.println("No Contacts Available.");
            return;
        }

        viewContacts();

        System.out.print("Enter Contact ID to Update: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (id < 1 || id > contacts.size()) {
            System.out.println("Invalid Contact ID!");
            return;
        }

        Contact c = contacts.get(id - 1);

        System.out.print("Enter New Name: ");
        c.name = sc.nextLine();

        System.out.print("Enter New Phone: ");
        c.phone = sc.nextLine();

        System.out.print("Enter New Email: ");
        c.email = sc.nextLine();

        System.out.println("Contact Updated Successfully!");
    }

    // Delete Contact
    static void deleteContact() {

        if (contacts.isEmpty()) {
            System.out.println("No Contacts Available.");
            return;
        }

        viewContacts();

        System.out.print("Enter Contact ID to Delete: ");
        int id = sc.nextInt();

        if (id < 1 || id > contacts.size()) {
            System.out.println("Invalid Contact ID!");
            return;
        }

        contacts.remove(id - 1);

        System.out.println("Contact Deleted Successfully!");
    }
}