package pvz.controller;

import pvz.model.auth.SecurityQuestions;
import pvz.model.auth.User;
import pvz.model.enums.Gender;
import pvz.model.enums.MenuContext;

import java.util.List;
import java.util.Random;

class AuthController {

    private final GameApp app;
    private final Random rng = new Random();
    private User pendingSecurityUser;
    private User recoveryUser;
    private String recoveryQuestion;
    private boolean recoveryVerified;

    AuthController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (app.current() == MenuContext.REGISTER) handleRegisterMenu(line);
        else handleLoginMenu(line);
    }
    private void handleRegisterMenu(String line) {
        if (line.equals("help")) return;

        if (line.startsWith("register ")) {
            register(line);
            return;}

        if (line.startsWith("pick question")) {
            pickQuestion(line);
            return;}

    }

    private void register(String line) {
        if (pendingSecurityUser != null) return;

        Args args = Args.parse(line);
        String username = args.get("u");
        String passwords = args.get("p");
        String nickname = args.get("n");
        String email = args.get("e");
        String gender = args.get("g");

        if (username == null || passwords == null || nickname == null || email == null || gender == null) {
            return;}

        String[] passwordParts = passwords.split("\\s+");
        if (passwordParts.length != 2) return;

        Gender parsedGender;
        try {
            parsedGender = Gender.valueOf(gender.trim().toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return;}

        try {
            User user = app.auth.register(username.trim(), passwordParts[0], passwordParts[1],
                    nickname.trim(), email.trim(), parsedGender);
            pendingSecurityUser = user;
            SecurityQuestions.printAll();
        }
        catch (IllegalArgumentException e) {}
    }

    private void pickQuestion(String line) {
        if (pendingSecurityUser == null) return;

        Args args = Args.parse(line);
        Integer number = args.getInt("q");
        String answer = args.get("a");
        String confirm = args.get("c");
        if (number == null || answer == null || confirm == null) return;

        if (number < 1 || number > SecurityQuestions.count()) return;

        if (!answer.equals(confirm)) return;

        pendingSecurityUser.addSecurityQA(SecurityQuestions.byNumber(number), app.auth.hashPassword(answer.toLowerCase()));
        app.saveAll();
        pendingSecurityUser = null;
        app.switchTo(MenuContext.LOGIN);
    }

    private void handleLoginMenu(String line) {
        if (line.equals("help")) return;

        if (line.startsWith("login ")) {
            login(line);
            return;}

        if (line.startsWith("forget password")) {
            forgetPassword(line);
            return;}

        if (line.startsWith("answer ")) {
            answer(line);
            return;}

        if (line.startsWith("reset password")) {
            resetPassword(line);
            return;}

    }

    private void login(String line) {
        Args args = Args.parse(line);
        String username = args.get("u");
        String password = args.get("p");
        boolean stay = args.has("stay-logged-in");
        if (username == null || password == null) return;

        password = password.replace("-stay-logged-in", "").trim();
        User user = app.auth.login(username.trim(), password, stay);
        if (user == null) return;
        app.onLoggedIn(user);
    }

    private void forgetPassword(String line) {
        Args args = Args.parse(line);
        String username = args.get("u");
        String email = args.get("e");
        if (username == null || email == null) return;
        User user = app.auth.findByUsername(username.trim());
        if (user == null || !user.getEmail().equalsIgnoreCase(email.trim())) return;

        List<String[]> qas = user.getSecurityQAs();
        if (qas.isEmpty()) return;

        String[] qa = qas.get(rng.nextInt(qas.size()));
        recoveryUser = user;
        recoveryQuestion = qa[0];
        recoveryVerified = false;
    }

    private void answer(String line) {
        if (recoveryUser == null) return;

        Args args = Args.parse(line);
        String answer = args.get("a");
        if (answer == null) return;

        String hash = app.auth.hashPassword(answer.trim().toLowerCase());
        boolean correct = recoveryUser.getSecurityQAs().stream().anyMatch(qa -> qa[0].equals(recoveryQuestion) && qa[1].equals(hash));
        if (!correct) {
            recoveryUser = null;
            return;}

        recoveryVerified = true;
    }

    private void resetPassword(String line) {
        if (recoveryUser == null || !recoveryVerified) return;

        Args args = Args.parse(line);
        String passwords = args.get("p");
        if (passwords == null) return;

        String[] parts = passwords.split("\\s+");
        if (parts.length != 2 || !parts[0].equals(parts[1])) return;

        List<String> problems = app.auth.passwordProblems(parts[0]);
        if (!problems.isEmpty()) return;

        recoveryUser.setPasswordHash(app.auth.hashPassword(parts[0]));
        app.saveAll();
        recoveryUser = null;
        recoveryVerified = false;
    }
}
