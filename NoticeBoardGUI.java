import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class NoticeBoardGUI {

    // -------------------------
    // Notice data model
    // -------------------------
    static class Notice {
        String title;
        String message;
        String category;
        String keyword;
        String eventDate;

        Notice(String t, String m, String c, String k, String d) {
            title = t;
            message = m;
            category = c;
            keyword = k;
            eventDate = d;
        }

        String toFileLine() {
            return title + "|" + message + "|" + category + "|" + keyword + "|" + eventDate;
        }

        static Notice fromFileLine(String line) {
            try {
                String[] p = line.split("\\|", 5);
                return new Notice(p[0], p[1], p[2], p[3], p[4]);
            } catch (Exception e) {
                return null;
            }
        }

        public String toString() {
            return "[" + category + "] " + title + " (" + keyword + ") - " + message + " <Event Date: " + eventDate + ">";
        }
    }

    // -------------------------
    // Custom Stack
    // -------------------------
    class MyStack {
        class Node {
            Notice data;
            Node next;
        }

        Node top;

        void push(Notice n) {
            Node newNode = new Node();
            newNode.data = n;
            newNode.next = top;
            top = newNode;
        }

        Notice pop() {
            if (top == null) return null;
            Notice temp = top.data;
            top = top.next;
            return temp;
        }

        boolean isEmpty() {
            return top == null;
        }

        java.util.List<Notice> toList() {
            java.util.List<Notice> list = new java.util.ArrayList<>();
            Node curr = top;
            while (curr != null) {
                list.add(curr.data);
                curr = curr.next;
            }
            return list;
        }
    }

    // -------------------------
    // Custom Queue
    // -------------------------
    class MyQueue {
        class Node {
            Notice data;
            Node next;
        }

        Node front, rear;

        void enqueue(Notice n) {
            Node newNode = new Node();
            newNode.data = n;
            if (rear == null) {
                front = rear = newNode;
            } else {
                rear.next = newNode;
                rear = newNode;
            }
        }

        Notice dequeue() {
            if (front == null) return null;
            Notice temp = front.data;
            front = front.next;
            if (front == null) rear = null;
            return temp;
        }

        boolean isEmpty() {
            return front == null;
        }

        java.util.List<Notice> toList() {
            java.util.List<Notice> list = new java.util.ArrayList<>();
            Node curr = front;
            while (curr != null) {
                list.add(curr.data);
                curr = curr.next;
            }
            return list;
        }
    }

    // -------------------------
    // Custom HashMap (simple)
    // -------------------------
    class MyMap {
        class Pair {
            String key;
            java.util.List<Notice> value;
            Pair next;
        }

        Pair[] table = new Pair[20];

        int hash(String key) {
            int sum = 0;
            for (char ch : key.toCharArray()) sum += ch;
            return sum % table.length;
        }

        void put(String key, java.util.List<Notice> value) {
            int index = hash(key);
            Pair newPair = new Pair();
            newPair.key = key;
            newPair.value = value;
            newPair.next = table[index];
            table[index] = newPair;
        }

        java.util.List<Notice> get(String key) {
            int index = hash(key);
            Pair current = table[index];
            while (current != null) {
                if (current.key.equals(key)) return current.value;
                current = current.next;
            }
            return new java.util.ArrayList<>();
        }

        boolean contains(String key) {
            int index = hash(key);
            Pair current = table[index];
            while (current != null) {
                if (current.key.equals(key)) return true;
                current = current.next;
            }
            return false;
        }

        void putIfAbsent(String key, java.util.List<Notice> value) {
            if (!contains(key)) put(key, value);
        }
    }

    // -------------------------
    // NoticeBoard (manages DSAs)
    // -------------------------
    class NoticeBoard {
        MyStack stack = new MyStack();
        MyQueue queue = new MyQueue();
        MyMap keywordMap = new MyMap();

        java.util.List<String> categories = Arrays.asList("Academic", "Events", "Scholarships", "Clubs", "General");
        File file = new File("notices.txt");

        NoticeBoard() {
            loadFromFile();
        }

        void addNotice(Notice n) {
            stack.push(n);
            queue.enqueue(n);
            String key = n.keyword.toLowerCase();

            if (!keywordMap.contains(key)) {
                java.util.List<Notice> list = new java.util.ArrayList<>();
                list.add(n);
                keywordMap.put(key, list);
            } else {
                keywordMap.get(key).add(n);
            }
            saveToFile();
        }

        java.util.List<Notice> getAll() {
            return queue.toList();
        }

        java.util.List<Notice> getRecent(int limit) {
            java.util.List<Notice> all = stack.toList();
            java.util.List<Notice> list = new java.util.ArrayList<>();
            for (int i = 0; i < all.size() && i < limit; i++) list.add(all.get(i));
            return list;
        }

        java.util.List<Notice> searchByKeyword(String k) {
            return keywordMap.get(k.toLowerCase());
        }

        java.util.List<Notice> getByCategory(String c) {
            java.util.List<Notice> list = new java.util.ArrayList<>();
            for (Notice n : queue.toList())
                if (n.category.equalsIgnoreCase(c)) list.add(n);
            return list;
        }

        void saveToFile() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (Notice n : queue.toList()) {
                    bw.write(n.toFileLine());
                    bw.newLine();
                }
            } catch (Exception e) {
                System.out.println("Save error: " + e.getMessage());
            }
        }

        void loadFromFile() {
            if (!file.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Notice n = Notice.fromFileLine(line);
                    if (n != null) {
                        stack.push(n);
                        queue.enqueue(n);
                        String k = n.keyword.toLowerCase();
                        if (!keywordMap.contains(k)) {
                            java.util.List<Notice> list = new java.util.ArrayList<>();
                            list.add(n);
                            keywordMap.put(k, list);
                        } else {
                            keywordMap.get(k).add(n);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Load error: " + e.getMessage());
            }
        }
    }

    // -------------------------
    // GUI SECTION
    // -------------------------
    private NoticeBoard board = new NoticeBoard();
    private JFrame mainFrame = new JFrame("Digital Notice Board Manager");
    private static final String ADMIN_PASS = "YWRtaW4xMjM="; // Base64("admin123")

    public NoticeBoardGUI() {
        SwingUtilities.invokeLater(this::mainMenu);
    }

    boolean checkPassword(String input) {
        String encoded = Base64.getEncoder().encodeToString(input.getBytes());
        return encoded.equals(ADMIN_PASS);
    }

    void mainMenu() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(420, 280);
        mainFrame.setLocationRelativeTo(null);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel title = new JLabel("<html><h2>Digital Notice Board</h2></html>", SwingConstants.CENTER);
        p.add(title, BorderLayout.NORTH);

        JButton adminBtn = new JButton("Administrator Login");
        JButton studentBtn = new JButton("Student Portal");
        JButton exitBtn = new JButton("Exit");

        JPanel center = new JPanel(new GridLayout(3, 1, 8, 8));
        center.add(adminBtn);
        center.add(studentBtn);
        center.add(exitBtn);
        p.add(center, BorderLayout.CENTER);

        adminBtn.addActionListener(e -> adminLogin());
        studentBtn.addActionListener(e -> studentPanel());
        exitBtn.addActionListener(e -> System.exit(0));

        mainFrame.setContentPane(p);
        mainFrame.setVisible(true);
    }

    void adminLogin() {
        JPasswordField pwd = new JPasswordField();
        Object[] msg = {"Enter Admin Passcode:", pwd};
        int opt = JOptionPane.showConfirmDialog(mainFrame, msg, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String input = new String(pwd.getPassword());
            if (checkPassword(input)) adminPanel();
            else JOptionPane.showMessageDialog(mainFrame, "Wrong Password!");
        }
    }

    void adminPanel() {
        JFrame f = new JFrame("Administrator Panel");
        f.setSize(900, 650);
        f.setLocationRelativeTo(mainFrame);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField title = new JTextField(20);
        JTextField keyword = new JTextField(10);
        JTextField dateField = new JTextField(10);
        JComboBox<String> cat = new JComboBox<>(board.categories.toArray(new String[0]));
        JTextArea msg = new JTextArea(3, 20);
        JButton addBtn = new JButton("Add Notice");
        JTextArea display = new JTextArea(15, 50);
        display.setEditable(false);
        display.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(display);
        scroll.setBorder(BorderFactory.createTitledBorder("All Notices (Chronological Order)"));
        scroll.setPreferredSize(new Dimension(750, 300));

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Add New Notice"));
        form.add(new JLabel("Title:"));
        form.add(title);
        form.add(new JLabel("Category:"));
        form.add(cat);
        form.add(new JLabel("Keyword:"));
        form.add(keyword);
        form.add(new JLabel("Event Date (dd-MM-yyyy):"));
        form.add(dateField);
        form.add(new JLabel("Message:"));
        form.add(new JScrollPane(msg));
        form.add(addBtn);

        main.add(form, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(logoutBtn);
        main.add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            String t = title.getText().trim();
            String m = msg.getText().trim();
            String k = keyword.getText().trim();
            String d = dateField.getText().trim();
            String c = (String) cat.getSelectedItem();

            if (t.isEmpty() || m.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(f, "All fields are required!");
                return;
            }

            Notice n = new Notice(t, m, c, k, d);
            board.addNotice(n);
            JOptionPane.showMessageDialog(f, "Notice Added!");
            display.setText(formatList(board.getAll()));
            title.setText("");
            msg.setText("");
            keyword.setText("");
            dateField.setText("");
        });

        logoutBtn.addActionListener(e -> {
            f.dispose();
            mainMenu();
        });

        f.setContentPane(main);
        f.setVisible(true);
    }

    void studentPanel() {
    JFrame f = new JFrame("Student Portal");
    f.setSize(850, 600);
    f.setLocationRelativeTo(mainFrame);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBorder(new EmptyBorder(10, 10, 10, 10));

    JTextArea display = new JTextArea();
    display.setEditable(false);
    display.setFont(new Font("Monospaced", Font.PLAIN, 12));
    JScrollPane scroll = new JScrollPane(display);
    scroll.setBorder(BorderFactory.createTitledBorder("Notices"));
    main.add(scroll, BorderLayout.CENTER);

    // --- Dropdown for category selection ---
    JComboBox<String> categoryBox = new JComboBox<>(board.categories.toArray(new String[0]));
    categoryBox.setSelectedIndex(-1); // nothing selected by default

    JButton allBtn = new JButton("📜 View All Notices");
    JButton recentBtn = new JButton("⏰ Recent 5 Notices");
    JButton exitBtn = new JButton("❌ Exit");

    // --- Styling ---
    Color blue = new Color(70, 130, 180);
    Color green = new Color(60, 179, 113);
    Color red = new Color(220, 20, 60);

    allBtn.setBackground(blue);
    allBtn.setForeground(Color.WHITE);
    recentBtn.setBackground(green);
    recentBtn.setForeground(Color.WHITE);
    exitBtn.setBackground(red);
    exitBtn.setForeground(Color.WHITE);

    // --- Top panel layout ---
    JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    top.add(new JLabel("Select Category:"));
    top.add(categoryBox);
    top.add(allBtn);
    top.add(recentBtn);
    main.add(top, BorderLayout.NORTH);

    // --- Bottom panel ---
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottom.add(exitBtn);
    main.add(bottom, BorderLayout.SOUTH);

    // --- Actions ---
    allBtn.addActionListener(e -> display.setText(formatList(board.getAll())));
    recentBtn.addActionListener(e -> display.setText(formatList(board.getRecent(5))));
    exitBtn.addActionListener(e -> {
        f.dispose();
        mainMenu();
    });

    // --- Show notices automatically when a category is selected ---
    categoryBox.addActionListener(e -> {
        String selectedCategory = (String) categoryBox.getSelectedItem();
        if (selectedCategory != null) {
            display.setText(formatList(board.getByCategory(selectedCategory)));
        }
    });

    f.setContentPane(main);
    f.setVisible(true);
}


    String formatList(java.util.List<Notice> list) {
        if (list == null || list.isEmpty()) return "No notices found.";
        StringBuilder s = new StringBuilder();
        int i = 1;
        for (Notice n : list) {
            s.append(i++).append(". ").append(n.toString()).append("\n\n");
        }
        return s.toString();
    }

    public static void main(String[] args) {
        new NoticeBoardGUI();
    }
}
