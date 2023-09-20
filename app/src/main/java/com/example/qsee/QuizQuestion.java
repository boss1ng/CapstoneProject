package com.example.qsee;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> answerChoices;
    private int correctAnswerIndex;
    private int imageResourceId;
    private String correctAnswerDescription;

    public QuizQuestion(String question, List<String> answerChoices, int correctAnswerIndex, int imageResourceId, String correctAnswerDescription) {
        this.question = question;
        this.answerChoices = answerChoices;
        this.correctAnswerIndex = correctAnswerIndex;
        this.imageResourceId = imageResourceId;
        this.correctAnswerDescription = correctAnswerDescription;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswerChoices() {
        return answerChoices;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getCorrectAnswerDescription() {
        return correctAnswerDescription;
    }
}

