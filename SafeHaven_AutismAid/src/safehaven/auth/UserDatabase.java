package safehaven.auth;

import safehaven.models.UserAccount;
import safehaven.models.UserAccount.Role;
import safehaven.utils.Logger;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class UserDatabase {
    private static UserDatabase instance;
    private final Map<String, UserAccount> accounts = new LinkedHashMap<>();
    private static final String DATA_FILE = System.getProperty("user.home") + "/.safehaven_users.dat";

    private UserDatabase() { load(); }

    public static synchronized UserDatabase getInstance() {
        if (instance == null) instance = new UserDatabase();
        return instance;
    }

    public boolean emailExists(String email) {
        if (email == null || email.isEmpty()) return false;
        return accounts.containsKey(email.toLowerCase().trim());
    }

    public UserAccount getAccount(String email) {
        if (email == null || email.isEmpty()) return null;
        return accounts.get(email.toLowerCase().trim());
    }

    public boolean authenticate(String email, String password) {
        if (email == null || password == null) return false;
        UserAccount acc = getAccount(email);
        if (acc == null) return false;
        return acc.getPasswordHash().equals(hashPassword(password));
    }

    public boolean register(String email, String password, String name, Role role) {
        if (email == null || password == null || name == null || role == null) {
            Logger.warn("Register called with null parameters");
            return false;
        }
        String key = email.toLowerCase().trim();
        if (accounts.containsKey(key)) {
            Logger.info("Register failed: email already exists: " + key);
            return false;
        }
        accounts.put(key, new UserAccount(key, hashPassword(password), name, role));
        save();
        Logger.info("User registered: " + key + " as " + role);
        return true;
    }

    /**
     * Parent can link a child: child must already have an account; child's role must be CHILD.
     * One parent can link multiple children (each must have their own email+password).
     * One therapist can be linked; therapist must use a different email.
     */
    public String linkChildToParent(String parentEmail, String childEmail, String childPassword) {
        if (parentEmail == null || childEmail == null || childPassword == null) {
            return "Invalid parameters.";
        }
        UserAccount parent = getAccount(parentEmail);
        UserAccount child  = getAccount(childEmail);
        if (parent == null) return "Parent account not found.";
        if (child  == null) return "Child account not found. Please register the child first.";
        if (child.getRole() != Role.CHILD) return "That account is not a Child account.";
        if (!authenticate(childEmail, childPassword)) return "Wrong password for child account.";
        if (parent.getLinkedEmails().contains(childEmail.toLowerCase()))
            return "Child already linked.";
        parent.addLinkedEmail(childEmail.toLowerCase());
        save();
        Logger.info("Child linked to parent: " + childEmail.toLowerCase());
        return "ok";
    }

    // ...existing code...

    public List<UserAccount> getLinkedChildren(String parentEmail) {
        UserAccount parent = getAccount(parentEmail);
        List<UserAccount> children = new ArrayList<>();
        if (parent == null) return children;
        for (String le : parent.getLinkedEmails()) {
            UserAccount la = getAccount(le);
            if (la != null && la.getRole() == Role.CHILD) children.add(la);
        }
        return children;
    }

    // ...existing code...

    public void save() {
        try {
            // Write to temp file first, then rename
            File tempFile = new File(DATA_FILE + ".tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeObject(new ArrayList<>(accounts.values()));
                oos.flush();
            }
            // Atomic rename (overwrites existing file)
            Files.move(tempFile.toPath(), Paths.get(DATA_FILE), StandardCopyOption.REPLACE_EXISTING);
            Logger.info("User database saved successfully (" + accounts.size() + " accounts)");
        } catch (IOException e) {
            Logger.error("Failed to save user database", e);
            // Don't throw; let app continue but warn user
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) {
            Logger.info("No user database found; starting with empty accounts");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            List<UserAccount> list = (List<UserAccount>) ois.readObject();
            if (list == null) {
                Logger.warn("Loaded null account list; starting fresh");
                return;
            }
            for (UserAccount ua : list) {
                if (ua != null && ua.getEmail() != null) {
                    accounts.put(ua.getEmail(), ua);
                }
            }
            Logger.info("User database loaded (" + accounts.size() + " accounts)");
        } catch (ClassNotFoundException e) {
            Logger.error("Account class format changed; cannot load old database", e);
        } catch (EOFException e) {
            Logger.error("User database file corrupted (EOF); starting fresh", e);
        } catch (IOException e) {
            Logger.error("Failed to load user database", e);
        }
    }

    public static String hashPassword(String password) {
        try {
            if (password == null || password.isEmpty()) {
                Logger.warn("Attempting to hash empty password");
                return "";
            }
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            Logger.error("Password hashing failed", e);
            return ""; // Return empty hash; login will fail, prompting user to retry
        }
    }
}
