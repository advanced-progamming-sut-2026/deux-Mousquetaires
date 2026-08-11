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
            app.view.info("Commands: show-info | change-username -u <new>" + " | change-nickname -u <nickname> | change-email -e <new>"
                    + " | change-password -p <new_password> -o <old_password> | menu exit");
            return;}

        if (line.equals("show-info")) {
            app.view.showProfile(user);
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

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void changeUsername(User user, String newUsername) {
        if (newUsername == null || newUsername.isEmpty()) {
            app.view.error("Usage: change-username -u <new username>");
            return;}
        if (newUsername.equals(user.getUsername())) {
            app.view.error("The new username is the same as your current username.");
            return;}

        ValidationResult validation = app.auth.validateUsername(newUsername);

        if (validation != ValidationResult.VALID) {
            app.view.error("Invalid username: letters, digits and '-' only.");
            return;}

        if (!app.auth.renameUser(user, newUsername)) {
            app.view.error("That username is already taken.");
            return;
        }
        app.view.success("Username changed to " + newUsername + ".");
        app.saveAll();
    }

    private void changeNickname(User user, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            app.view.error("Usage: change-nickname -u <new nickname>");
            return;}

        String trimmed = nickname.trim();
        if (trimmed.equals(user.getNickname())) {
            app.view.error("The new nickname is the same as your current nickname.");
            return;}

        if (trimmed.length() < 3) {
            app.view.error("Nickname must be at least 3 characters long.");
            return;}

        if (trimmed.length() > 30) {
            app.view.error("Nickname must be at most 30 characters long.");
            return;}

        user.setNickname(trimmed);
        app.view.success("Nickname changed to " + trimmed + ".");
        app.saveAll();}

    private void changeEmail(User user, String newEmail) {
        if (newEmail == null || newEmail.isEmpty()) {
            app.view.error("Usage: change-email -e <new email>");
            return;}

        String trimmed = newEmail.trim();
        if (trimmed.equalsIgnoreCase(user.getEmail())) {
            app.view.error("The new email is the same as your current email.");
            return;}

        if (app.auth.validateEmail(trimmed) != ValidationResult.VALID) {
            app.view.error("That email address is not valid.");
            return;}

        user.setEmail(trimmed);
        app.view.success("Email changed to " + trimmed + ".");
        app.saveAll();
    }

    private void changePassword(User user, String newPassword, String oldPassword) {
        if (newPassword == null || newPassword.isEmpty() || oldPassword == null || oldPassword.isEmpty()) {
            app.view.error("Usage: change-password -p <new_password> -o <old_password>");
            return;}
        if (!user.checkPassword(oldPassword, app.auth)) {
            app.view.error("The old password is incorrect.");
            return;}
        if (newPassword.equals(oldPassword)) {
            app.view.error("The new password is the same as your current password.");
            return;}
        List<String> problems = app.auth.passwordProblems(newPassword);
        if (!problems.isEmpty()) {
            problems.forEach(app.view::error);
            return;}
        user.setPasswordHash(app.auth.hashPassword(newPassword));
        app.view.success("Password changed.");
        app.saveAll();
    }
}
