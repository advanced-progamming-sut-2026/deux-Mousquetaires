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
        for (int i = 0; i < QUESTIONS.length; i++) {
            }
    }
}
