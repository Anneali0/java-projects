/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.student;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {

    static final int MAX_LIST = 20;      // reading list size
    static final int MAX_CATALOG = 30;   // fetched books size

    // [i][0] = title, [i][1] = author, [i][2] = url
    static String[][] catalog = new String[MAX_CATALOG][3];
    static int catalogCount = 0;
    static String[][] readingList = new String[MAX_LIST][3];
    static int readingCount = 0;

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;
        do {
            printMenu();
            choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    exploreBooks();
                    break;
                case 2:
                    System.out.print("Enter title: ");
                    searchBook(sc.nextLine().trim());
                    break;
                case 3:
                    System.out.print("Enter author: ");
                    searchBook(sc.nextLine().trim(), true);
                    break;
                case 4:
                    addBook();
                    break;
                case 5:
                    viewReadingList();
                    break;
                case 6:
                    removeBook();
                    break;
                case 7:
                    displayBookDetails();
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 0-7.");
            }
        } while (choice != 0);
    }

    static void printMenu() {
        System.out.println();
        System.out.println("===== GUTENBERG BOOK EXPLORER =====");
        System.out.println("1. Explore Books");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.println("4. Add Book to Reading List");
        System.out.println("5. View Reading List");
        System.out.println("6. Remove Book");
        System.out.println("7. Display Book Details");
        System.out.println("0. Exit");
    }

    // Reads an integer safely; returns -1 if input is not a number
    static int readInt(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ---------- Explore ----------
    static void exploreBooks() {
        System.out.println("Loading books...");
        try {
            Document doc = Jsoup.connect("https://www.gutenberg.org/browse/scores/top")
                    .userAgent("Mozilla/5.0").timeout(15000).get();
            catalogCount = 0;
            Elements links = doc.select("a[href^=/ebooks/]");
            for (Element a : links) {
                if (catalogCount >= MAX_CATALOG) break;
                String href = a.attr("href");
                if (!href.matches("/ebooks/\\d+")) continue; // skip non-book links
                String url = a.absUrl("href");
                if (alreadyInCatalog(url)) continue;         // skip duplicates

                String text = a.text().replaceAll("\\s*\\(\\d+\\)\\s*$", "").trim();
                String title = text;
                String author = "Unknown";
                int idx = text.lastIndexOf(" by ");
                if (idx > 0) {
                    title = text.substring(0, idx).trim();
                    author = text.substring(idx + 4).trim();
                }
                catalog[catalogCount][0] = title;
                catalog[catalogCount][1] = author;
                catalog[catalogCount][2] = url;
                catalogCount++;
            }
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            return;
        }
        showCatalog();
    }

    static boolean alreadyInCatalog(String url) {
        for (int i = 0; i < catalogCount; i++) {
            if (catalog[i][2].equals(url)) return true;
        }
        return false;
    }

    // ---------- Search (overloaded) ----------
    static void searchBook(String title) {
        searchBook(title, false);
    }

    static void searchBook(String keyword, boolean searchByAuthor) {
        if (keyword.isEmpty()) {
            System.out.println("Search text cannot be empty.");
            return;
        }
        System.out.println("Searching...");
        try {
            String query = (searchByAuthor ? "a." : "t.") + keyword;
            String url = "https://www.gutenberg.org/ebooks/search/?query="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(15000).get();
            catalogCount = 0;
            Elements items = doc.select("li.booklink");
            for (Element li : items) {
                if (catalogCount >= MAX_CATALOG) break;
                Element link = li.selectFirst("a.link");
                Element t = li.selectFirst("span.title");
                Element a = li.selectFirst("span.subtitle");
                if (link == null || t == null) continue;
                catalog[catalogCount][0] = t.text();
                catalog[catalogCount][1] = (a != null) ? a.text() : "Unknown";
                catalog[catalogCount][2] = link.absUrl("href");
                catalogCount++;
            }
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            return;
        }
        if (catalogCount == 0) {
            System.out.println("No results found.");
            return;
        }
        showCatalog();
    }

    static void showCatalog() {
        if (catalogCount == 0) {
            System.out.println("No books found.");
            return;
        }
        System.out.println("\n--- Books ---");
        for (int i = 0; i < catalogCount; i++) {
            System.out.println((i + 1) + ". " + catalog[i][0] + " | " + catalog[i][1]);
            System.out.println("   " + catalog[i][2]);
        }
    }

    // ---------- Reading list ----------
    static void addBook() {
        if (catalogCount == 0) {
            System.out.println("No books loaded. Use Explore or Search first.");
            return;
        }
        if (readingCount >= MAX_LIST) {
            System.out.println("Reading list is full (" + MAX_LIST + " books).");
            return;
        }
        showCatalog();
        int n = readInt("Enter the book number to add: ");
        if (n < 1 || n > catalogCount) {
            System.out.println("Invalid book number.");
            return;
        }
        String[] book = catalog[n - 1];
        for (int i = 0; i < readingCount; i++) {
            if (readingList[i][2].equals(book[2])) {
                System.out.println("This book is already in your reading list.");
                return;
            }
        }
        readingList[readingCount][0] = book[0];
        readingList[readingCount][1] = book[1];
        readingList[readingCount][2] = book[2];
        readingCount++;
        System.out.println("Added: " + book[0]);
    }

    static void viewReadingList() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }
        System.out.println("\n--- Your Reading List ---");
        for (int i = 0; i < readingCount; i++) {
            System.out.println((i + 1) + ". " + readingList[i][0] + " | " + readingList[i][1]);
        }
    }

    static void removeBook() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }
        viewReadingList();
        int n = readInt("Enter the book number to remove: ");
        if (n < 1 || n > readingCount) {
            System.out.println("Invalid book number.");
            return;
        }
        String removed = readingList[n - 1][0];
        for (int i = n - 1; i < readingCount - 1; i++) {
            readingList[i] = readingList[i + 1];
        }
        readingList[readingCount - 1] = new String[3];
        readingCount--;
        System.out.println("Removed: " + removed);
    }

    static void displayBookDetails() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }
        viewReadingList();
        int n = readInt("Enter the book number to see details: ");
        if (n < 1 || n > readingCount) {
            System.out.println("Invalid book number.");
            return;
        }
        System.out.println("\n--- Book Details ---");
        System.out.println("Title : " + readingList[n - 1][0]);
        System.out.println("Author: " + readingList[n - 1][1]);
        System.out.println("URL   : " + readingList[n - 1][2]);
    }
}
