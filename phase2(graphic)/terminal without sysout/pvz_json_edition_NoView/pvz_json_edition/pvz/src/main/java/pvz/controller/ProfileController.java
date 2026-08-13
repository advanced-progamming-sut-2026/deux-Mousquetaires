package pvz.controller;

import pvz.model.auth.User;
import pvz.model.enums.ValidationResult;

import java.util.List;

class ProfileController {

    private final GameApp app;

    ProfileController(GameApp app) {
        this.app = app;
    }

    void handle(String line) {
        User user = app.currentUser;
        if (line.equals("help")) {
            return;}

        if (line.equals("show-info")) {
            return;}

        Args args = Args.parse(line);
        if (line.startsWith("change-username")) {
            changeUsername(user, args.get("u"));
            return;}

        if (line.startsWith("change-nickname")) {
            changeNickname(user, args.get("u") != null ? args.get("u") : args.get("n"));
            return;}

        if (line.startsWith("change-email")) {
            changeEmail(user, args.get("e"));
            return;}

        if (line.startsWith("change-password")) {
            changePassword(user, args.get("p"), args.get("o"));
            return;}

    }

    private void changeUsername(User user, String newUsername) {
        if (newUsername == null || newUsername.isEmpty()) {
            return;}
        if (newUsername.equals(user.getUsername())) {
            return;}

        ValidationResult validation = app.auth.validateUsername(newUsername);

        if (validation != ValidationResult.VALID) {
            return;}

        if (!app.auth.renameUser(user, newUsername)) {
            return;
        }
        app.saveAll();
    }

    private void changeNickname(User user, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return;}

        String trimmed = nickname.trim();
        if (trimmed.equals(user.getNickname())) {
            return;}

        if (trimmed.length() < 3) {
            return;}

        if (trimmed.length() > 30) {
            return;}

        user.setNickname(trimmed);
        app.saveAll();}

    private void changeEmail(User user, String newEmail) {
        if (newEmail == null || newEmail.isEmpty()) {
            return;}

        String trimmed = newEmail.trim();
        if (trimmed.equalsIgnoreCase(user.getEmail())) {
            return;}

        if (app.auth.validateEmail(trimmed) != ValidationResult.VALID) {
            return;}

        user.setEmail(trimmed);
        app.saveAll();
    }

    private void changePassword(User user, String newPassword, String oldPassword) {
        if (newPassword == null || newPassword.isEmpty() || oldPassword == null || oldPassword.isEmpty()) {
            return;}
        if (!user.checkPassword(oldPassword, app.auth)) {
            return;}
        if (newPassword.equals(oldPassword)) {
            return;}
        List<String> problems = app.auth.passwordProblems(newPassword);
        if (!problems.isEmpty()) {
            return;}
        user.setPasswordHash(app.auth.hashPassword(newPassword));
        app.saveAll();
    }
}
