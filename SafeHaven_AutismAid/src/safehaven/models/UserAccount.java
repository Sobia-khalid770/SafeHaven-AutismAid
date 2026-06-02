package safehaven.models;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role { PARENT, CHILD }  // Removed THERAPIST

    // ...existing code...

    private String email;
    private String passwordHash;
    private String name;
    private Role role;
    private String profilePicPath;
    private String about;
    private List<String> linkedEmails;
    private Map<String, Integer> progress;

    public UserAccount(String email, String passwordHash, String name, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.linkedEmails = new ArrayList<>();
        this.progress = new ConcurrentHashMap<>();
    }

    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName()         { return name; }
    public Role   getRole()         { return role; }
    public String getProfilePic()   { return profilePicPath; }
    public String getAbout()        { return about; }
    public List<String> getLinkedEmails() {
        if (linkedEmails == null) linkedEmails = new ArrayList<>();
        return linkedEmails;
    }
    public Map<String, Integer> getProgress() {
        if (progress == null) progress = new ConcurrentHashMap<>();
        return progress;
    }
    public void setName(String n)            { this.name = n; }
    public void setProfilePic(String path)   { this.profilePicPath = path; }
    public void setAbout(String about)       { this.about = about; }
    public void setPasswordHash(String hash) { this.passwordHash = hash; }

    public void addLinkedEmail(String email) {
        if (linkedEmails == null) linkedEmails = new ArrayList<>();
        if (email != null && !linkedEmails.contains(email)) linkedEmails.add(email);
    }

    /**
     * Thread-safe progress update. Keeps the maximum score achieved.
     */
    public void updateProgress(String activity, int score) {
        if (progress == null) progress = new ConcurrentHashMap<>();
        if (activity == null) return;
        progress.put(activity, Math.max(score, progress.getOrDefault(activity, 0)));
        safehaven.auth.UserDatabase.getInstance().save();
    }
}
