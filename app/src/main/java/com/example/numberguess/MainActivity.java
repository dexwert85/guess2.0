package com.example.numberguess;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private GuessGame game;
    private TextView welcomeTextView;
    private TextView scoreTextView;
    private TextView attemptsTextView;
    private TextView timerTextView;
    private Spinner difficultySpinner;
    private EditText pickEditText;
    private Button submitButton;
    private CountDownTimer countDownTimer;
    private boolean isTimerStarted = false; // Prevents timer from restarting on every keypress
    private int remainingSeconds = 0;      // Tracks remaining seconds to calculate bonus points
    private DBHelper db;
    private ModelUser currentUser;        // Holds currently logged-in user object
    private Button highScoresButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind layout XML views to Java variables
        welcomeTextView = findViewById(R.id.welcome);
        scoreTextView = findViewById(R.id.score);
        attemptsTextView = findViewById(R.id.attempts);
        timerTextView = findViewById(R.id.timer);
        difficultySpinner = findViewById(R.id.spinner);
        pickEditText = findViewById(R.id.pick);
        submitButton = findViewById(R.id.submit);
        highScoresButton = findViewById(R.id.btn_high_scores);

        // Instantiate game model with initial EASY difficulty
        game = new GuessGame(GuessGame.Difficulty.EASY);
        db = new DBHelper(this);

        // Populate Difficulty Spinner with Enum values (EASY, MEDIUM, HARD)
        ArrayAdapter<GuessGame.Difficulty> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                GuessGame.Difficulty.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        // Trigger new game whenever user picks a different difficulty from spinner
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GuessGame.Difficulty selectedDifficulty = (GuessGame.Difficulty) parent.getItemAtPosition(position);
                startNewGame(selectedDifficulty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Listen to user typing: starts countdown on the very first character entered
        pickEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Start timer only if it hasn't started yet, input field has text, and game is active
                if (!isTimerStarted && s.length() > 0 && !game.isGameOver()) {
                    isTimerStarted = true;
                    startTimer();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Process guess attempt when Submit button is clicked
        submitButton.setOnClickListener(v -> handleGuess());

        highScoresButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Score.class);
            startActivity(intent);
            finish();
        });

        // Prompt user to log in or sign up when activity launches
        showLoginDialog();
    }

    /**
     * Displays custom login dialog inflated from XML layout.
     */
    private void showLoginDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.login_dialog, null);

        EditText etUsername = dialogView.findViewById(R.id.et_login_username);
        EditText etPassword = dialogView.findViewById(R.id.et_login_password);
        Button btnLogin = dialogView.findViewById(R.id.login);
        Button btnSignUp = dialogView.findViewById(R.id.sign_up);

        btnLogin.setText("Login");
        btnSignUp.setText("Sign Up");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.checkUser(username, password)) {
                ArrayList<ModelUser> users = db.genericSelectByUserName(username);
                if (!users.isEmpty()) {
                    currentUser = users.get(0);
                    updateWelcomeHeader();
                }
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        btnSignUp.setOnClickListener(v -> {
            dialog.dismiss();
            showSignUpDialog();
        });

        dialog.show();
    }

    /**
     * Displays custom sign up dialog with password validation rule checks.
     */
    private void showSignUpDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.signup_dialog, null);

        EditText etUsername = dialogView.findViewById(R.id.et_signup_username);
        EditText etName = dialogView.findViewById(R.id.et_signup_name);
        EditText etPassword = dialogView.findViewById(R.id.et_signup_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_signup_confirm_password);
        Button btnSubmit = dialogView.findViewById(R.id.btn_signup_submit);
        Button btnBack = dialogView.findViewById(R.id.btn_signup_back);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnSubmit.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPassword(password)) {
                Toast.makeText(this, "Password must be at least 6 characters long, contain at least 1 uppercase letter, and 1 number", Toast.LENGTH_LONG).show();
                return;
            }

            if (db.registerUser(username, name, password)) {
                ArrayList<ModelUser> users = db.genericSelectByUserName(username);
                if (!users.isEmpty()) {
                    currentUser = users.get(0);
                } else {
                    currentUser = new ModelUser(username, name, password, 0, 0);
                }
                updateWelcomeHeader();
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            showLoginDialog();
        });

        dialog.show();
    }

    /**
     * Updates header TextView displaying "hello {name}".
     */
    private void updateWelcomeHeader() {
        if (currentUser != null) {
            welcomeTextView.setText("hello " + currentUser.getName());
        }
    }

    /**
     * Validates password rules:
     * - Minimum 6 characters
     * - At least 1 uppercase letter
     * - At least 1 digit/number
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasUppercase && hasDigit;
    }

    /**
     * Updates the database with the highest score using the logged-in currentUser model.
     */
    private void updateDB() {
        if (currentUser == null) return;

        int currentScore = game.getTotalScore();

        if (currentScore > currentUser.getScore()) {
            currentUser.setScore(currentScore);
            db.update(currentUser);
            Toast.makeText(this, "New High Score Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Resets game logic, cancels running timers, clears flags, and updates the UI for a new round.
     *
     * @param difficulty Selected difficulty level for the new round.
     */
    private void startNewGame(GuessGame.Difficulty difficulty) {
        cancelTimer();
        isTimerStarted = false;
        remainingSeconds = difficulty.getSeconds();
        game.resetGame(difficulty);
        updateUiState();
    }

    /**
     * Initializes and starts the CountDownTimer object.
     * Decrements seconds each tick and triggers time-out loss on finish.
     */
    private void startTimer() {
        cancelTimer(); // Stop any active countdown thread

        long totalMillis = game.getCurrentDifficulty().getSeconds() * 1000L;

        countDownTimer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSeconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText("Time: " + remainingSeconds + "s");
            }

            @Override
            public void onFinish() {
                remainingSeconds = 0;
                timerTextView.setText("Time: 0s");
                game.timeout();
                showGameOverDialog("Time's Up! ⏰", "You ran out of time! The target number was: " + game.getTargetNumber());
            }
        }.start();
    }

    /**
     * Synchronizes UI fields (score, attempts left, timer, hint) with current game state.
     */
    private void updateUiState() {
        scoreTextView.setText("Score: " + game.getTotalScore());
        attemptsTextView.setText("Attempts Left: " + game.getRemainingAttempts());
        timerTextView.setText("Time: " + game.getCurrentDifficulty().getSeconds() + "s");
        pickEditText.setHint("Pick a number from 1-" + game.getCurrentDifficulty().getMaxRandom());
        pickEditText.setText("");
    }

    /**
     * Reads and validates input, evaluates guess via GuessGame engine,
     * updates attempts, and displays hints or win/loss dialogs.
     */
    private void handleGuess() {
        if (game.isGameOver()) return;

        String input = pickEditText.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int userGuess = Integer.parseInt(input);
        String result = game.makeGuess(userGuess);

        // Update remaining attempts text after guess submission
        attemptsTextView.setText("Attempts Left: " + game.getRemainingAttempts());

        switch (result) {
            case "Win":
                cancelTimer();
                int earnedPoints = game.addWinScore(remainingSeconds);
                scoreTextView.setText("Score: " + game.getTotalScore());
                updateDB();
                showGameOverDialog("You Won! 🎉", "Earned " + earnedPoints + " points!\nTotal Score: " + game.getTotalScore());
                break;

            case "Lose":
                cancelTimer();
                showGameOverDialog("Game Over ❌", "You ran out of attempts! The number was: " + game.getTargetNumber());
                break;

            case "Higher":
                Toast.makeText(this, "Try Higher!", Toast.LENGTH_SHORT).show();
                break;

            case "Lower":
                Toast.makeText(this, "Try Lower!", Toast.LENGTH_SHORT).show();
                break;
        }

        pickEditText.setText("");
    }

    /**
     * Creates and presents a non-dismissible AlertDialog displaying win/lose results
     * and a button to restart the game.
     *
     * @param title   Dialog title header text.
     * @param message Body text showing final scores or answer details.
     */
    private void showGameOverDialog(String title, String message) {
        cancelTimer();

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    GuessGame.Difficulty currentDifficulty = (GuessGame.Difficulty) difficultySpinner.getSelectedItem();
                    startNewGame(currentDifficulty);
                })
                .show();
    }

    /**
     * Safely stops the CountDownTimer object if active.
     */
    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /**
     * Lifecycle callback ensuring background timers are terminated when Activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }
}