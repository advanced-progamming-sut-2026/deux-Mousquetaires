package pvz.view;

class MessageView {

    void info(String message) {
        System.out.println(message);
    }

    void error(String message) {
        System.out.println("[!] " + message);
    }

    void success(String message) {
        System.out.println("[+] " + message);
    }

    void menuPath(String name) {
        System.out.println("You are in the " + name + ".");
    }

    void prompt(String menuName) {
        System.out.print("[" + menuName + "] > ");
    }
}
