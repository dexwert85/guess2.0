package com.example.numberguess;

import java.util.Random;

public class GuessGame {
    public enum Difficulty {
        EASY(50, 10, 30),
        MEDIUM(100, 7, 20),
        HARD(200, 5, 10);

        private final int maxRandom;
        private final int defaultAttempts;
        private final int seconds;

        Difficulty(int maxRandom, int defaultAttempts, int seconds) {
            this.maxRandom = maxRandom;
            this.defaultAttempts = defaultAttempts;
            this.seconds = seconds;
        }

        public int generateTargetNumber(Random rand) {
            return rand.nextInt(this.maxRandom) + 1;
        }

        public int getAttempts() {
            return this.defaultAttempts;
        }

        public int getMaxRandom() {
            return maxRandom;
        }

        public int getSeconds() {
            return seconds;
        }
    }
    private int targetNumber;
    private int remainingAttempts;
    private boolean isGameOver;
    private Difficulty currentDifficulty;
    private Random rand;
    private int totalScore = 0;

    public GuessGame(Difficulty difficulty) {
        rand = new Random();
        isGameOver = false;
        currentDifficulty = difficulty;
        targetNumber = currentDifficulty.generateTargetNumber(rand);
        remainingAttempts = currentDifficulty.getAttempts();
    }

    public void resetGame(Difficulty difficulty) {
        isGameOver = false;
        currentDifficulty = difficulty;
        targetNumber = currentDifficulty.generateTargetNumber(rand);
        remainingAttempts = currentDifficulty.getAttempts();
    }

    public String makeGuess(int userGuess) {
        if (isGameOver) return "Game Over";

        remainingAttempts--;

        if (userGuess > targetNumber) {
            return "Lower";
        } else if (userGuess < targetNumber) {
            return "Higher";
        } else {
            return "Win";
        }
    }

    public int addWinScore(int remainingSeconds) {
        int roundScore = (remainingAttempts * 10) + (remainingSeconds * 5);
        totalScore += roundScore;
        return roundScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void timeout() {
        isGameOver = true;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }
}