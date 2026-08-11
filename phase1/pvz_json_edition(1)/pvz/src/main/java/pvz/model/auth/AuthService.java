package pvz.model.auth;

import pvz.model.enums.Gender;
import pvz.model.enums.ValidationResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuthService {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int MIN_PASSWORD_LEN = 8;
    private final Map<String, User> userStore = new LinkedHashMap<>();

    public User register(String username, String password, String passwordConfirm,
                         String nickname, String email, Gender gender) {
        ValidationResult usernameCheck = validateUsername(username);
        if (usernameCheck == ValidationResult.ALREADY_TAKEN) {
            throw new IllegalArgumentException("Username " + username + " is already taken. Please pick another one.");
        }
        if (usernameCheck != ValidationResult.VALID) {
            throw new IllegalArgumentException("Username format is invalid. Use letters, digits and '-' only.");
        }

        List<String> passwordIssues = passwordProblems(password);
        if (!passwordIssues.isEmpty()) {
            throw new IllegalArgumentException("Password is weak: " + String.join(", ", passwordIssues) + ".");
        }
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("Password and its confirmation do not match. Please try again.");
        }
        if (validateEmail(email) != ValidationResult.VALID) {
            throw new IllegalArgumentException("Email address format is invalid.");
        }
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("This email address is already registered.");
        }
        if (nickname == null || nickname.trim().length() < 3) {
            throw new IllegalArgumentException("Nickname must be at least 3 characters long.");
        }
        if (nickname.trim().length() > 30) {
            throw new IllegalArgumentException("Nickname must be at most 30 characters long.");
        }
        User user = new User(username, hashPassword(password), email, nickname, gender);
        userStore.put(username.toLowerCase(), user);
        return user;
    }

    public User login(String username, String password, boolean stay) {
        User user = findByUsername(username);
        if (user == null || !user.checkPassword(password, this)) return null;
        if (stay) user.generateSessionToken();
        return user;
    }

    public ValidationResult validateUsername(String username) {
        if (username == null || username.isEmpty()) return ValidationResult.TOO_SHORT;
        if (!username.matches("[A-Za-z0-9-]+")) return ValidationResult.INVALID_FORMAT;
        if (isUsernameTaken(username)) return ValidationResult.ALREADY_TAKEN;
        return ValidationResult.VALID;
    }

    public List<String> passwordProblems(String password) {
        List<String> problems = new ArrayList<>();
        if (password == null || password.length() < MIN_PASSWORD_LEN) {
            problems.add("it must be at least " + MIN_PASSWORD_LEN + " characters");
            return problems;
        }
        if (password.chars().noneMatch(Character::isUpperCase)) problems.add("it needs an uppercase letter");
        if (password.chars().noneMatch(Character::isLowerCase)) problems.add("it needs a lowercase letter");
        if (password.chars().noneMatch(Character::isDigit)) problems.add("it needs a digit");
        if (password.chars().allMatch(c -> Character.isLetterOrDigit(c))) problems.add("it needs a special character");
        return problems;
    }

    public ValidationResult validateEmail(String email) {
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) return ValidationResult.INVALID_FORMAT;
        return ValidationResult.VALID;
    }
    public boolean isUsernameTaken(String username) {
        return userStore.containsKey(username.toLowerCase());
    }

    //change
    public boolean renameUser(User user, String newUsername) {
        if (validateUsername(newUsername) != ValidationResult.VALID) return false;
        userStore.remove(user.getUsername().toLowerCase());
        user.renameTo(newUsername);
        userStore.put(newUsername.toLowerCase(), user);
        return true;
    }

    //Hash
    public String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hash) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    //serach and find
    public User findByUsername(String username) {
        return username == null ? null : userStore.get(username.toLowerCase());
    }

    public User findByEmail(String email) {
        return userStore.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }
    public List<User> getAllUsers() {
        return new ArrayList<>(userStore.values());
    }
    public void restoreUser(User user) {
        userStore.put(user.getUsername().toLowerCase(), user);
    }
}
