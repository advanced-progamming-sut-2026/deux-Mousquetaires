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
        if (line.equals("help")) {
            app.view.info("Commands: register -u <user> -p <pass> <confirm> -n <nick>" + " -e <email> -g <gender> | pick question -q <n> -a <answer> -c <confirm>" + " | menu enter login | menu exit");
            return;}

        if (line.startsWith("register ")) {
            register(line);
            return;}

        if (line.startsWith("pick question")) {
            pickQuestion(line);
            return;}

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void register(String line) {
        if (pendingSecurityUser != null) {
            app.view.error("Finish your registration first: pick question -q <n> -a <answer> -c <confirm>");
            return;}

        Args args = Args.parse(line);
        String username = args.get("u");
        String passwords = args.get("p");
        String nickname = args.get("n");
        String email = args.get("e");
        String gender = args.get("g");

        if (username == null || passwords == null || nickname == null || email == null || gender == null) {
            app.view.error("Usage: register -u <user> -p <pass> <confirm> -n <nick>" + " -e <email> -g <gender>");
            return;}

        String[] passwordParts = passwords.split("\\s+");
        if (passwordParts.length != 2) {
            app.view.error("Provide the password and its confirmation after -p.");
            return;}

        Gender parsedGender;
        try {
            parsedGender = Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            app.view.error("Gender must be one of: male, female, other");
            return;}

        try {
            User user = app.auth.register(username.trim(), passwordParts[0], passwordParts[1],
                    nickname.trim(), email.trim(), parsedGender);
            pendingSecurityUser = user;
            app.view.success("Account created! Now pick a security question:");
            SecurityQuestions.printAll();
            app.view.info("Use: pick question -q <number> -a <answer> -c <answer-confirm>");
        } catch (IllegalArgumentException e) {
            app.view.error(e.getMessage());
        }
    }

    private void pickQuestion(String line) {
        if (pendingSecurityUser == null) {
            app.view.error("Register first, then pick your security question.");
            return;}

        Args args = Args.parse(line);
        Integer number = args.getInt("q");
        String answer = args.get("a");
        String confirm = args.get("c");
        if (number == null || answer == null || confirm == null) {
            app.view.error("Usage: pick question -q <number> -a <answer> -c <answer-confirm>");
            return;}

        if (number < 1 || number > SecurityQuestions.count()) {
            app.view.error("Question number must be 1.." + SecurityQuestions.count());
            return;}

        if (!answer.equals(confirm)) {
            app.view.error("The answer and its confirmation do not match. Please try again.");
            return;}

        pendingSecurityUser.addSecurityQA(SecurityQuestions.byNumber(number),
                app.auth.hashPassword(answer.toLowerCase()));
        app.view.success("Security question saved. Registration complete - you can log in now.");
        app.saveAll();
        pendingSecurityUser = null;
        app.switchTo(MenuContext.LOGIN);
    }

    private void handleLoginMenu(String line) {
        if (line.equals("help")) {
            app.view.info("Commands: login -u <user> -p <pass> [-stay-logged-in]" + " | forget password -u <user> -e <email> | answer -a <answer>" + " | reset password -p <new> <confirm> | menu enter register | menu exit");
            return;}

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

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void login(String line) {
        Args args = Args.parse(line);
        String username = args.get("u");
        String password = args.get("p");
        boolean stay = args.has("stay-logged-in");
        if (username == null || password == null) {
            app.view.error("Usage: login -u <user> -p <pass> [-stay-logged-in]");
            return;}

        password = password.replace("-stay-logged-in", "").trim();
        User user = app.auth.login(username.trim(), password, stay);
        if (user == null) {
            app.view.error("Username or password is incorrect.");
            return;}
        app.onLoggedIn(user);
    }

    private void forgetPassword(String line) {
        Args args = Args.parse(line);
        String username = args.get("u");
        String email = args.get("e");
        if (username == null || email == null) {
            app.view.error("Usage: forget password -u <user> -e <email>");
            return;}
        User user = app.auth.findByUsername(username.trim());
        if (user == null || !user.getEmail().equalsIgnoreCase(email.trim())) {
            app.view.error("No account matches that username and email.");
            return;
        }
        List<String[]> qas = user.getSecurityQAs();
        if (qas.isEmpty()) {
            app.view.error("This account has no security question; the password cannot be recovered.");
            return;}

        String[] qa = qas.get(rng.nextInt(qas.size()));
        recoveryUser = user;
        recoveryQuestion = qa[0];
        recoveryVerified = false;
        app.view.info("Security question: " + recoveryQuestion);
        app.view.info("Reply with: answer -a <your answer>");
    }

    private void answer(String line) {
        if (recoveryUser == null) {
            app.view.error("Start with: forget password -u <user> -e <email>");
            return;}

        Args args = Args.parse(line);
        String answer = args.get("a");
        if (answer == null) {
            app.view.error("Usage: answer -a <your answer>");
            return;}

        String hash = app.auth.hashPassword(answer.trim().toLowerCase());
        boolean correct = recoveryUser.getSecurityQAs().stream().anyMatch(qa -> qa[0].equals(recoveryQuestion) && qa[1].equals(hash));
        if (!correct) {
            app.view.error("Wrong answer. Start again with: forget password -u <user> -e <email>");
            recoveryUser = null;
            return;}

        recoveryVerified = true;
        app.view.success("Correct! Now use: reset password -p <new password> <confirm>");
    }

    private void resetPassword(String line) {
        if (recoveryUser == null || !recoveryVerified) {
            app.view.error("Answer your security question first.");
            return;}

        Args args = Args.parse(line);
        String passwords = args.get("p");
        if (passwords == null) {
            app.view.error("Usage: reset password -p <new password> <confirm>");
            return;}

        String[] parts = passwords.split("\\s+");
        if (parts.length != 2 || !parts[0].equals(parts[1])) {
            app.view.error("Password and its confirmation do not match. Please try again.");
            return;}

        List<String> problems = app.auth.passwordProblems(parts[0]);
        if (!problems.isEmpty()) {
            problems.forEach(app.view::error);
            return;}

        recoveryUser.setPasswordHash(app.auth.hashPassword(parts[0]));
        app.view.success("Password changed! You can log in now.");
        app.saveAll();
        recoveryUser = null;
        recoveryVerified = false;
    }
}
