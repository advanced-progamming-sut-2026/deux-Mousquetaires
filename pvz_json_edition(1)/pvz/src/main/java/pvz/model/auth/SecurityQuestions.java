package pvz.model.auth;

public final class SecurityQuestions {

    private static final String[] QUESTIONS = {
            "What is your best friend's name?",
            "What was the name of your first pet?",
            "What is your favourite plant?",
            "What city were you born in?",
            "What was your childhood nickname?",
            "What is your favourite food?",
    };

    private SecurityQuestions() {}
    public static int count() {
        return QUESTIONS.length;
    }
    public static String byNumber(int number) {
        if (number < 1 || number > QUESTIONS.length) return null;
        return QUESTIONS[number - 1];
    }

    public static void printAll() {
        System.out.println("Pick a security question with: pick question -q <number> -a <answer> -c <answer-confirmation>");
        for (int i = 0; i < QUESTIONS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + QUESTIONS[i]);}
    }
}
