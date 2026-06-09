package safehaven.auth;

import safehaven.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OTP verification service.
 * Real email sending requires javax.mail (mail.jar) on the classpath AND the
 * environment variables SH_EMAIL and SH_PASS set to a Gmail account that has
 * "App Passwords" enabled (2-FA + App Password, NOT your real password).
 * Without those env vars, the OTP is printed to System.out and shown in a
 * dialog (test / offline mode)  perfectly usable during development.
 */
public class EmailService {
    private static EmailService instance;
    private final Map<String, OTPRecord> pending = new ConcurrentHashMap<>();

    private static final String SENDER_EMAIL =
        System.getenv("SH_EMAIL") != null ? System.getenv("SH_EMAIL") : "";
    private static final String SENDER_PASS  =
        System.getenv("SH_PASS")  != null ? System.getenv("SH_PASS")  : "";
    private static final long   OTP_TTL_MS   = 10 * 60 * 1000L; // 10 minutes

    private EmailService() {}

    public static synchronized EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    //  Public API 

    /**
     * Generates a 4-digit OTP for the given email and attempts to send it.
     * @return "sent"     email dispatched successfully
     *         "console"  SMTP not configured; code printed to console instead
     */
    public String generateAndSend(String toEmail) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            Logger.warn("OTP requested for null/empty email");
            return "error";
        }
        
        String code = String.format("%04d", new Random().nextInt(10000));
        pending.put(key(toEmail), new OTPRecord(code, System.currentTimeMillis()));

        if (!SENDER_EMAIL.isEmpty() && !SENDER_PASS.isEmpty()) {
            try {
                sendViaReflection(toEmail, code);
                Logger.info("OTP sent to email: " + toEmail);
                return "sent";
            } catch (Exception e) {
                Logger.error("SMTP send failed for " + toEmail, e);
                // Fall through to console mode
            }
        }

        System.out.println("[SafeHaven OTP] " + toEmail + " => " + code);
        Logger.info("OTP displayed in console (SMTP not configured): " + toEmail);
        return "console";
    }

    /**
     * Returns true and removes the OTP if the code matches and is not expired.
     */
    public boolean verify(String email, String code) {
        if (email == null || code == null) {
            Logger.warn("OTP verify called with null email or code");
            return false;
        }
        
        OTPRecord rec = pending.get(key(email));
        if (rec == null) {
            Logger.warn("OTP verification failed: no record for " + email);
            return false;
        }
        if (System.currentTimeMillis() - rec.timestamp > OTP_TTL_MS) {
            pending.remove(key(email));
            Logger.warn("OTP verification failed: expired for " + email);
            return false;
        }
        if (rec.code.equals(code.trim())) {
            pending.remove(key(email));
            Logger.info("OTP verified successfully for " + email);
            return true;
        }
        Logger.warn("OTP verification failed: wrong code for " + email);
        return false;
    }

    /** Used by SignUpFrame to show the code in a dialog when SMTP is off. */
    public String getPendingCode(String email) {
        if (email == null) return null;
        OTPRecord r = pending.get(key(email));
        return r != null ? r.code : null;
    }

    //  SMTP via reflection (avoids compile-time javax.mail dependency) 

    private void sendViaReflection(String to, String code) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        final String user = SENDER_EMAIL;
        final String pass = SENDER_PASS;

        // javax.mail classes loaded dynamically
        Class<?> cSession  = Class.forName("javax.mail.Session");
        Class<?> cAuth     = Class.forName("javax.mail.Authenticator");
        Class<?> cPassAuth = Class.forName("javax.mail.PasswordAuthentication");
        Class<?> cMime     = Class.forName("javax.mail.internet.MimeMessage");
        Class<?> cMessage  = Class.forName("javax.mail.Message");
        Class<?> cRT       = Class.forName("javax.mail.Message$RecipientType");
        Class<?> cIA       = Class.forName("javax.mail.internet.InternetAddress");
        Class<?> cTrans    = Class.forName("javax.mail.Transport");

        // Build Authenticator via anonymous subclass (reflection-safe approach)
        Object authenticator = java.lang.reflect.Proxy.newProxyInstance(
            cAuth.getClassLoader(),
            new Class<?>[]{ cAuth.getInterfaces().length > 0
                ? cAuth.getInterfaces()[0] : cAuth },
            (proxy, method, args) -> {
                if ("getPasswordAuthentication".equals(method.getName()))
                    return cPassAuth.getConstructor(String.class, String.class)
                                    .newInstance(user, pass);
                return null;
            });

        // Create session with authenticator
        Object session = cSession.getMethod("getInstance", Properties.class, cAuth)
                                  .invoke(null, props, authenticator);

        // Build message
        Object msg  = cMime.getConstructor(cSession).newInstance(session);
        Object from = cIA.getConstructor(String.class).newInstance(user);
        cMessage.getMethod("setFrom", Class.forName("javax.mail.Address"))
                .invoke(msg, from);

        Object toAddr = cIA.getMethod("parse", String.class).invoke(null, to);
        Object toType = cRT.getField("TO").get(null);
        cMessage.getMethod("setRecipients", cRT, toAddr.getClass())
                .invoke(msg, toType, toAddr);

        cMessage.getMethod("setSubject", String.class)
                .invoke(msg, "SafeHaven  Your Verification Code");
        cMessage.getMethod("setText", String.class)
                .invoke(msg, "Assalam o Alaikum!\n\n" +
                             "Your SafeHaven verification code is:\n\n    " + code +
                             "\n\nValid for 10 minutes.\n\n SafeHaven Team");

        // Send
        cTrans.getMethod("send", cMessage).invoke(null, msg);
    }

    //  Helpers 

    private static String key(String e) {
        return e == null ? "" : e.toLowerCase().trim();
    }

    private static class OTPRecord {
        final String code;
        final long   timestamp;
        OTPRecord(String code, long timestamp) {
            this.code      = code;
            this.timestamp = timestamp;
        }
    }
}
