package com.greengrocer.utils;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Utility class for evaluating and displaying password strength in JavaFX
 * applications.
 * <p>
 * This class provides sophisticated password strength analysis using multiple
 * criteria:
 * </p>
 * <ul>
 * <li>Length scoring (0-5 points)</li>
 * <li>Character variety (lowercase, uppercase, digits, special chars - 0-4
 * points)</li>
 * <li>Common weak password detection (heavy penalty)</li>
 * <li>Sequential character detection (123, abc, xyz)</li>
 * <li>Repeated character detection (111, aaa)</li>
 * <li>Keyboard pattern detection (qwerty, asdf)</li>
 * <li>Single character type penalties (only numbers, only letters)</li>
 * </ul>
 * <p>
 * <strong>Strength levels:</strong>
 * </p>
 * <ul>
 * <li><strong>WEAK (0-3 points):</strong> Vulnerable password</li>
 * <li><strong>NORMAL (4-7 points):</strong> Acceptable but could be
 * stronger</li>
 * <li><strong>STRONG (8+ points):</strong> Excellent security</li>
 * </ul>
 * 
 * @author Burak Özevin
 */
public class PasswordStrengthUtil {

    /**
     * Enumeration of password strength levels.
     * 
     * @author Burak Özevin
     */
    public enum StrengthLevel {
        /** Weak password (score 0-3) */
        WEAK,
        /** Normal password (score 4-7) */
        NORMAL,
        /** Strong password (score 8+) */
        STRONG
    }

    /**
     * Container class for password strength evaluation results.
     * <p>
     * Encapsulates the strength level, numeric score, and descriptive message.
     * </p>
     * 
     * @author Burak Özevin
     */
    public static class PasswordStrength {
        /** The strength level (WEAK, NORMAL, STRONG) */
        private final StrengthLevel level;

        /** Numeric score (0-10+) */
        private final int score;

        /** Descriptive message with weakness reasons or success message */
        private final String message;

        /**
         * Constructs a PasswordStrength result.
         * 
         * @param level   the strength level
         * @param score   the numeric score
         * @param message the descriptive message
         * @author Burak Özevin
         */
        public PasswordStrength(StrengthLevel level, int score, String message) {
            this.level = level;
            this.score = score;
            this.message = message;
        }

        /**
         * Gets the strength level.
         * 
         * @return the strength level (WEAK, NORMAL, STRONG)
         * @author Burak Özevin
         */
        public StrengthLevel getLevel() {
            return level;
        }

        /**
         * Gets the numeric score.
         * 
         * @return the score (0-10+)
         * @author Burak Özevin
         */
        public int getScore() {
            return score;
        }

        /**
         * Gets the descriptive message.
         * 
         * @return the message explaining weaknesses or confirming strength
         * @author Burak Özevin
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Common weak passwords and patterns to check against
     */
    private static final String[] COMMON_WEAK_PASSWORDS = {
            "password", "123456", "12345678", "qwerty", "abc123", "monkey", "1234567",
            "letmein", "trustno1", "dragon", "baseball", "111111", "iloveyou", "master",
            "sunshine", "ashley", "bailey", "passw0rd", "shadow", "123123", "654321",
            "superman", "qazwsx", "michael", "football", "welcome", "jesus", "ninja",
            "mustang", "password1", "123456789", "admin", "root", "toor", "pass",
            "test", "guest", "info", "adm", "mysql", "user", "administrator", "oracle",
            "ftp", "pi", "puppet", "ansible", "ec2-user", "vagrant", "azureuser"
    };

    /**
     * Evaluates password strength using sophisticated multi-criteria analysis.
     * <p>
     * This method performs comprehensive password analysis with the following
     * scoring:
     * </p>
     * <ol>
     * <li><strong>Length (0-5 points):</strong> &lt;6=0, 6-7=1, 8-11=2, 12-15=3,
     * 16-19=4, 20+=5</li>
     * <li><strong>Character variety (0-4 points):</strong> +1 for each: lowercase,
     * uppercase, digit, special</li>
     * <li><strong>Common passwords:</strong> -5 points if contains weak password
     * from database</li>
     * <li><strong>Sequential chars:</strong> -3 points for sequences like 123, abc,
     * xyz</li>
     * <li><strong>Repeated chars:</strong> -2 points for patterns like 111,
     * aaa</li>
     * <li><strong>Keyboard patterns:</strong> -3 points for qwerty, asdf, zxcv,
     * etc.</li>
     * <li><strong>Only numbers:</strong> -2 points</li>
     * <li><strong>Only letters:</strong> -1 point</li>
     * </ol>
     * <p>
     * <strong>Final scoring:</strong>
     * </p>
     * <ul>
     * <li>0-3: WEAK</li>
     * <li>4-7: NORMAL</li>
     * <li>8+: STRONG</li>
     * </ul>
     * 
     * @param password the password to evaluate
     * @return PasswordStrength object with level, score, and detailed message
     * @author Burak Özevin
     */
    public static PasswordStrength evaluatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(StrengthLevel.WEAK, 0, "Password cannot be empty");
        }

        int score = 0;
        StringBuilder weaknessReasons = new StringBuilder();

        // 1. Length scoring (max 6 points now to reward longer passwords)
        int length = password.length();
        if (length < 6) {
            weaknessReasons.append("Too short. ");
            score += 0;
        } else if (length < 8) {
            score += 1;
            weaknessReasons.append("Short length. ");
        } else if (length < 12) {
            score += 3; // Increased from 2
        } else if (length < 16) {
            score += 6; // Increased from 3 to 6 (major boost for 12+ chars)
        } else if (length < 20) {
            score += 7; // Increased from 4
        } else {
            score += 8; // Increased from 5
        }

        // 2. Character variety (max 4 points)
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (hasLower)
            score += 1;
        if (hasUpper)
            score += 1;
        if (hasDigit)
            score += 1;
        if (hasSpecial)
            score += 1;

        if (!hasLower && !hasUpper)
            weaknessReasons.append("No letters. ");
        if (!hasDigit)
            weaknessReasons.append("No numbers. ");
        if (!hasSpecial)
            weaknessReasons.append("No special chars. ");

        // 3. Check for common weak passwords (CRITICAL - major penalty)
        String lowerPassword = password.toLowerCase();
        for (String weak : COMMON_WEAK_PASSWORDS) {
            if (lowerPassword.contains(weak)) {
                score = Math.max(0, score - 5); // Heavy penalty
                weaknessReasons.append("Contains common word '").append(weak).append("'. ");
                break;
            }
        }

        // 4. Check for sequential characters (123, abc, qwe, etc.)
        if (hasSequentialChars(password)) {
            score = Math.max(0, score - 3);
            weaknessReasons.append("Sequential characters detected. ");
        }

        // 5. Check for repeated characters (111, aaa, etc.)
        if (hasRepeatedChars(password)) {
            score = Math.max(0, score - 2);
            weaknessReasons.append("Repeated characters. ");
        }

        // 6. Check for keyboard patterns (qwerty, asdf, zxcv, etc.)
        if (hasKeyboardPattern(password)) {
            score = Math.max(0, score - 3);
            weaknessReasons.append("Keyboard pattern detected. ");
        }

        // 7. Check if password is only numbers (STRICT CHECK)
        if (password.matches("^\\d+$")) {
            // Force score down to ensure WEAK level regardless of length
            score = Math.min(score, 3);
            weaknessReasons.append("Only numbers (insecure). ");
        }

        // 8. Check if password is only letters
        if (password.matches("^[a-zA-Z]+$")) {
            score = Math.max(0, score - 1);
            weaknessReasons.append("Only letters. ");
        }

        // Determine strength level and message
        StrengthLevel level;
        String message;

        if (score <= 3) {
            level = StrengthLevel.WEAK;
            message = "Weak";
        } else if (score <= 7) {
            level = StrengthLevel.NORMAL;
            message = "Normal";
        } else {
            level = StrengthLevel.STRONG;
            message = "Strong";
        }

        return new PasswordStrength(level, score, message);
    }

    /**
     * Creates a JavaFX VBox containing password strength indicator UI components.
     * Includes a progress bar and a label showing the strength level and message.
     * 
     * @param password the password to evaluate
     * @return VBox containing password strength UI components
     */
    public static VBox createPasswordStrengthIndicator(String password) {
        VBox container = new VBox(5);
        container.setStyle("-fx-padding: 5 0 0 0;");

        PasswordStrength strength = evaluatePassword(password);

        // Progress bar
        ProgressBar progressBar = new ProgressBar();
        double progress = Math.max(0.05, Math.min(1.0, strength.getScore() / 10.0));
        progressBar.setProgress(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);

        // Set color based on strength level
        String progressBarStyle = "-fx-accent: ";
        switch (strength.getLevel()) {
            case WEAK:
                progressBarStyle += "#f44336"; // Red
                break;
            case NORMAL:
                progressBarStyle += "#ff9800"; // Orange/Yellow
                break;
            case STRONG:
                progressBarStyle += "#4caf50"; // Green
                break;
        }
        progressBar.setStyle(progressBarStyle + ";");

        // Label for strength level and message
        Label strengthLabel = new Label();
        strengthLabel.setWrapText(true);
        strengthLabel.setStyle("-fx-font-size: 11px;");

        String labelText = "";
        String labelColor = "";
        switch (strength.getLevel()) {
            case WEAK:
                labelText = "Weak";
                labelColor = "#f44336"; // Red
                break;
            case NORMAL:
                labelText = "Normal";
                labelColor = "#ff9800"; // Orange
                break;
            case STRONG:
                labelText = "Strong";
                labelColor = "#4caf50"; // Green
                break;
        }

        strengthLabel.setText(labelText);
        strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + labelColor + ";");

        container.getChildren().addAll(progressBar, strengthLabel);
        return container;
    }

    /**
     * Updates an existing VBox container with password strength information.
     * Useful for real-time feedback as the user types.
     * 
     * @param container the VBox container to update
     * @param password  the password to evaluate
     */
    public static void updatePasswordStrengthIndicator(VBox container, String password) {
        container.getChildren().clear();

        if (password == null || password.isEmpty()) {
            return;
        }

        PasswordStrength strength = evaluatePassword(password);

        // Progress bar
        ProgressBar progressBar = new ProgressBar();
        double progress = Math.max(0.05, Math.min(1.0, strength.getScore() / 10.0));
        progressBar.setProgress(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);

        // Set color based on strength level
        String progressBarStyle = "-fx-accent: ";
        switch (strength.getLevel()) {
            case WEAK:
                progressBarStyle += "#f44336"; // Red
                break;
            case NORMAL:
                progressBarStyle += "#ff9800"; // Orange/Yellow
                break;
            case STRONG:
                progressBarStyle += "#4caf50"; // Green
                break;
        }
        progressBar.setStyle(progressBarStyle + ";");

        // Label for strength level and message
        Label strengthLabel = new Label();
        strengthLabel.setWrapText(true);
        strengthLabel.setStyle("-fx-font-size: 11px;");

        String labelText = "";
        String labelColor = "";
        switch (strength.getLevel()) {
            case WEAK:
                labelText = "Weak";
                labelColor = "#f44336"; // Red
                break;
            case NORMAL:
                labelText = "Normal";
                labelColor = "#ff9800"; // Orange
                break;
            case STRONG:
                labelText = "Strong";
                labelColor = "#4caf50"; // Green
                break;
        }

        strengthLabel.setText(labelText);
        strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + labelColor + ";");

        container.getChildren().addAll(progressBar, strengthLabel);
    }

    /**
     * Checks for sequential characters in the password.
     * <p>
     * Detects common sequential patterns including:
     * </p>
     * <ul>
     * <li>Numeric sequences: 0123, 1234, 2345, ... 9876, 8765, etc.</li>
     * <li>Alphabetic sequences: abcd, bcde, cdef, ... zyxw, yxwv, etc.</li>
     * </ul>
     * 
     * @param password the password to check
     * @return true if sequential characters are detected, false otherwise
     * @author Burak Özevin
     */
    private static boolean hasSequentialChars(String password) {
        String lower = password.toLowerCase();

        // Check for numeric sequences
        String[] numSequences = { "0123", "1234", "2345", "3456", "4567", "5678", "6789",
                "9876", "8765", "7654", "6543", "5432", "4321", "3210" };
        for (String seq : numSequences) {
            if (lower.contains(seq))
                return true;
        }

        // Check for alphabetic sequences
        String[] alphaSequences = { "abcd", "bcde", "cdef", "defg", "efgh", "fghi", "ghij",
                "hijk", "ijkl", "jklm", "klmn", "lmno", "mnop", "nopq",
                "opqr", "pqrs", "qrst", "rstu", "stuv", "tuvw", "uvwx",
                "vwxy", "wxyz", "zyxw", "yxwv", "xwvu", "wvut", "vuts",
                "utsr", "tsrq", "srqp", "rqpo", "qpon", "ponm", "onml",
                "nmlk", "mlkj", "lkji", "kjih", "jihg", "ihgf", "hgfe",
                "gfed", "fedc", "edcb", "dcba" };
        for (String seq : alphaSequences) {
            if (lower.contains(seq))
                return true;
        }

        return false;
    }

    /**
     * Checks for repeated characters in the password.
     * <p>
     * Detects patterns where the same character appears 3 or more times
     * consecutively (e.g., "111", "aaa", "!!!").
     * </p>
     * 
     * @param password the password to check
     * @return true if repeated characters are detected, false otherwise
     */
    private static boolean hasRepeatedChars(String password) {
        // Check for 3 or more repeated characters
        return password.matches(".*(.)\\1{2,}.*");
    }

    /**
     * Checks for common keyboard patterns in the password.
     * <p>
     * Detects comprehensive keyboard patterns including:
     * </p>
     * <ul>
     * <li>Horizontal patterns: qwerty, asdfgh, zxcvbn (forward and reverse)</li>
     * <li>Vertical patterns: qaz, wsx, edc, rfv, tgb, yhn, ujm (forward and
     * reverse)</li>
     * <li>Diagonal patterns: qazwsx, edcrfv, tgbyhn (forward and reverse)</li>
     * <li>Number row patterns: 1234567890 (forward and reverse)</li>
     * <li>Alternative layouts: qwertz, azerty, dvorak patterns</li>
     * <li>Short patterns: 3+ consecutive keys in any direction</li>
     * </ul>
     * 
     * @param password the password to check
     * @return true if keyboard pattern is detected, false otherwise
     * @author Burak Özevin
     */
    private static boolean hasKeyboardPattern(String password) {
        String lower = password.toLowerCase();

        // Horizontal patterns (rows) - QWERTY layout
        String[] horizontalPatterns = {
                // Top number row
                "1234567890", "234567890", "34567890", "4567890", "567890", "67890", "7890", "890",
                "0987654321", "987654321", "87654321", "7654321", "654321", "54321", "4321", "321",
                "123", "234", "345", "456", "567", "678", "789", "098", "987", "876", "765", "654", "543", "432",

                // Top letter row (QWERTY)
                "qwertyuiop", "wertyuiop", "ertyuiop", "rtyuiop", "tyuiop", "yuiop", "uiop",
                "poiuytrewq", "oiuytrewq", "iuytrewq", "uytrewq", "ytrewq", "trewq", "rewq",
                "qwe", "wer", "ert", "rty", "tyu", "yui", "uio", "iop",
                "poi", "oiu", "iuy", "uyt", "ytr", "tre", "rew", "ewq",
                "qwer", "wert", "erty", "rtyu", "tyui", "yuio", "uiop",
                "poiu", "oiuy", "iuyt", "uytr", "ytre", "trew", "rewq",
                "qwerty", "wertyu", "ertyui", "rtyuio", "tyuiop",
                "poiuyt", "oiuytr", "iuytre", "uytrewq",

                // Middle row (ASDF)
                "asdfghjkl", "sdfghjkl", "dfghjkl", "fghjkl", "ghjkl", "hjkl",
                "lkjhgfdsa", "kjhgfdsa", "jhgfdsa", "hgfdsa", "gfdsa", "fdsa",
                "asd", "sdf", "dfg", "fgh", "ghj", "hjk", "jkl",
                "lkj", "kjh", "jhg", "hgf", "gfd", "fds", "dsa",
                "asdf", "sdfg", "dfgh", "fghj", "ghjk", "hjkl",
                "lkjh", "kjhg", "jhgf", "hgfd", "gfds", "fdsa",
                "asdfg", "sdfgh", "dfghj", "fghjk", "ghjkl",
                "lkjhg", "kjhgf", "jhgfd", "hgfds", "gfdsa",
                "asdfgh", "sdfghj", "dfghjk", "fghjkl",
                "lkjhgf", "kjhgfd", "jhgfds", "hgfdsa",

                // Bottom row (ZXCV)
                "zxcvbnm", "xcvbnm", "cvbnm", "vbnm", "bnm",
                "mnbvcxz", "nbvcxz", "bvcxz", "vcxz", "cxz",
                "zxc", "xcv", "cvb", "vbn", "bnm",
                "mnb", "nbv", "bvc", "vcx", "cxz",
                "zxcv", "xcvb", "cvbn", "vbnm",
                "mnbv", "nbvc", "bvcx", "vcxz",
                "zxcvb", "xcvbn", "cvbnm",
                "mnbvc", "nbvcx", "bvcxz",
                "zxcvbn", "xcvbnm",
                "mnbvcx", "nbvcxz"
        };

        // Vertical patterns (columns)
        String[] verticalPatterns = {
                // Left to right columns
                "qaz", "wsx", "edc", "rfv", "tgb", "yhn", "ujm", "ik", "ol",
                "zaq", "xsw", "cde", "vfr", "bgt", "nhy", "mju", "ki", "lo",
                "qazw", "wsxe", "edcr", "rfvt", "tgby", "yhnu", "ujmi", "iko", "olp",
                "wazq", "esxw", "rdce", "tfvr", "ygbt", "uhny", "imju", "oki", "plo",
                "qazws", "wsxed", "edcrf", "rfvtg", "tgbyh", "yhnuj", "ujmik", "ikol",
                "swzaq", "dexsw", "frced", "gtrfv", "hbygt", "junhy", "kimju", "loki"
        };

        // Diagonal patterns
        String[] diagonalPatterns = {
                // Top-left to bottom-right diagonals
                "qwsa", "weds", "erdf", "rtfg", "tygh", "yuhj", "uijk", "iopkl",
                "aswq", "sdew", "dfre", "fgtr", "ghty", "hjyu", "jkui", "lkio",
                "qazwsx", "wsxedc", "edcrfv", "rfvtgb", "tgbyhn", "yhnujm",
                "zaqxsw", "xswcde", "cdervf", "vfrtgb", "bgtyhn", "nhymju",
                "qwaszx", "wesdxc", "erdfcv", "rtfgvb", "tyghbn", "yuhjnm",
                "zxsawq", "xcdsew", "cvfdre", "vbgftr", "bnhgty", "nmjhyu",

                // Additional diagonal combinations
                "147", "258", "369", "159", "357", "753", "951", "963", "852", "741"
        };

        // Alternative keyboard layouts
        String[] alternativeLayouts = {
                // QWERTZ (German)
                "qwertz", "wertz", "qwert", "yxcv",
                "ztrewq", "ztrewq", "trewq",

                // AZERTY (French)
                "azerty", "zerty", "azer", "qsdfg",
                "ytreza", "ytreza", "treza",

                // Dvorak patterns
                "aoeu", "htns", "pyfg", "qjkx",
                "ueoa", "snth", "gfyp", "xkjq"
        };

        // Check all pattern arrays
        for (String pattern : horizontalPatterns) {
            if (lower.contains(pattern))
                return true;
        }

        for (String pattern : verticalPatterns) {
            if (lower.contains(pattern))
                return true;
        }

        for (String pattern : diagonalPatterns) {
            if (lower.contains(pattern))
                return true;
        }

        for (String pattern : alternativeLayouts) {
            if (lower.contains(pattern))
                return true;
        }

        return false;
    }
}
