package safehaven.utils;

/**
 * Text-to-Speech using Windows SAPI (System.Speech) via PowerShell.
 * Provides real English speech for letters, words, and numbers.
 * Falls back to SoundManager tones if SAPI is unavailable.
 */
public class TextToSpeech {

    /**
     * Speaks a letter and its associated word.
     * Example: speakLetter("A") says "A. A for Apple"
     */
    public static void speakLetter(String letter) {
        if (letter == null || letter.isEmpty()) return;

        letter = letter.toUpperCase();
        String word = getWordForLetter(letter.charAt(0));

        // Say: "A ... A for Apple"
        String text = letter + " ... " + letter + " for " + word;
        speak(text);
    }

    /**
     * Speaks a number with its associated counting object.
     * Example: speakNumber(3, "Stars") says "Three ... Three Stars"
     * This matches the visual emoji display in the UI.
     */
    public static void speakNumber(int num, String objectName) {
        if (num < 1 || num > 20) return;

        String[] names = {
            "", "One", "Two", "Three", "Four", "Five",
            "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
            "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty"
        };

        String text;
        if (objectName != null && !objectName.isEmpty()) {
            text = names[num] + " ... " + names[num] + " " + objectName;
        } else {
            text = names[num];
        }
        speak(text);

        // Extra cheer for 20
        if (num == 20) {
            SoundManager.getInstance().playCheer();
        }
    }

    /**
     * Speaks a number in English (backward-compatible overload).
     * Example: speakNumber(3) says "Three"
     */
    public static void speakNumber(int num) {
        speakNumber(num, null);
    }

    /**
     * Speaks arbitrary text using Windows SAPI via PowerShell.
     * Runs asynchronously on a background thread.
     */
    public static void speak(String text) {
        if (text == null || text.isEmpty()) return;

        ThreadPoolManager.execute(() -> {
            try {
                // Sanitize text: remove characters that could break PowerShell
                String safeText = text.replace("'", "")
                                      .replace("\"", "")
                                      .replace("`", "")
                                      .replace("$", "")
                                      .replace("&", "and")
                                      .replace("|", "")
                                      .replace(";", "")
                                      .replace("\n", " ")
                                      .replace("\r", " ");

                ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-NoProfile", "-Command",
                    "Add-Type -AssemblyName System.Speech; " +
                    "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$synth.Rate = -1; " +  // Slightly slower for children
                    "$synth.Speak('" + safeText + "'); " +
                    "$synth.Dispose()"
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // Read output to prevent process blocking
                try (java.io.InputStream is = process.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    while (is.read(buffer) != -1) {
                        // just read to clear buffer
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    Logger.warn("TTS process exited with code: " + exitCode + " for text: " + safeText);
                    // Fallback to tone-based sound
                    fallbackTone(text);
                }
            } catch (Exception e) {
                Logger.error("TTS failed, using fallback tones", e);
                fallbackTone(text);
            }
        });
    }

    /**
     * Fallback: play a simple tone sequence when SAPI is unavailable.
     */
    private static void fallbackTone(String text) {
        SoundManager sm = SoundManager.getInstance();
        sm.playTone(440, 200, 0.7f);
        sm.delay(100);
        sm.playTone(520, 150, 0.6f);
    }

    /**
     * Maps each letter to a common English word (matching LetterLearningGame data).
     */
    private static String getWordForLetter(char c) {
        switch (c) {
            case 'A': return "Apple";
            case 'B': return "Balloon";
            case 'C': return "Car";
            case 'D': return "Dog";
            case 'E': return "Egg";
            case 'F': return "Flower";
            case 'G': return "Grapes";
            case 'H': return "House";
            case 'I': return "Ice Cream";
            case 'J': return "Jar";
            case 'K': return "Key";
            case 'L': return "Lion";
            case 'M': return "Moon";
            case 'N': return "Nest";
            case 'O': return "Orange";
            case 'P': return "Pencil";
            case 'Q': return "Queen";
            case 'R': return "Rainbow";
            case 'S': return "Sun";
            case 'T': return "Tree";
            case 'U': return "Umbrella";
            case 'V': return "Vegetable";
            case 'W': return "Water";
            case 'X': return "X Mark";
            case 'Y': return "Yarn";
            case 'Z': return "Zebra";
            default: return "";
        }
    }
}
