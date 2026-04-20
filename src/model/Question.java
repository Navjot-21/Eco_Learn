package model;

/**
 * POJO representing a row in the {@code questions} table.
 * Converted from the embedded questionSchema in the MongoDB Quiz model.
 */
public class Question {

    private int    id;
    private int    quizId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int    correctOption;   // 0=A, 1=B, 2=C, 3=D
    private String explanation;
    private int    points;

    // ── Constructors ──────────────────────────────────────────────────

    public Question() {}

    public Question(int quizId, String questionText,
                    String optionA, String optionB,
                    String optionC, String optionD,
                    int correctOption, String explanation, int points) {
        this.quizId        = quizId;
        this.questionText  = questionText;
        this.optionA       = optionA;
        this.optionB       = optionB;
        this.optionC       = optionC;
        this.optionD       = optionD;
        this.correctOption = correctOption;
        this.explanation   = explanation;
        this.points        = points;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }

    public int    getQuizId()                    { return quizId; }
    public void   setQuizId(int quizId)          { this.quizId = quizId; }

    public String getQuestionText()              { return questionText; }
    public void   setQuestionText(String text)   { this.questionText = text; }

    public String getOptionA()                   { return optionA; }
    public void   setOptionA(String a)           { this.optionA = a; }

    public String getOptionB()                   { return optionB; }
    public void   setOptionB(String b)           { this.optionB = b; }

    public String getOptionC()                   { return optionC; }
    public void   setOptionC(String c)           { this.optionC = c; }

    public String getOptionD()                   { return optionD; }
    public void   setOptionD(String d)           { this.optionD = d; }

    public int    getCorrectOption()                  { return correctOption; }
    public void   setCorrectOption(int correctOption) { this.correctOption = correctOption; }

    public String getExplanation()               { return explanation; }
    public void   setExplanation(String exp)     { this.explanation = exp; }

    public int    getPoints()                    { return points; }
    public void   setPoints(int points)          { this.points = points; }

    /** Returns the option text corresponding to the correct answer index. */
    public String getCorrectOptionText() {
        switch (correctOption) {
            case 0: return optionA;
            case 1: return optionB;
            case 2: return optionC;
            case 3: return optionD;
            default: return "Unknown";
        }
    }

    /** Returns the option text for a given index (0-3). */
    public String getOptionByIndex(int index) {
        switch (index) {
            case 0: return optionA;
            case 1: return optionB;
            case 2: return optionC;
            case 3: return optionD;
            default: return "";
        }
    }

    @Override
    public String toString() {
        return "Question{id=" + id + ", quiz=" + quizId +
               ", text='" + questionText + "', points=" + points + "}";
    }
}
