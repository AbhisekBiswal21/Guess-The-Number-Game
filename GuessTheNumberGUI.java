import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.*;

public class GuessTheNumberGUI extends JFrame {
    private int numberToGuess, attempts, maxAttempts, lowerBound, upperBound;
    private int gamesPlayed = 0, wins = 0, bestAttempts = Integer.MAX_VALUE;

    private JTextField guessField;
    private JLabel messageLabel, attemptsLabel, hintLabel, scoreboardLabel;
    private JButton guessButton, restartButton, quitButton;
    private JProgressBar attemptsBar;
    private Random rand = new Random();
    private final String SCORE_FILE = "scoreboard.txt";

    public GuessTheNumberGUI() {
        loadScoreboard();
        setTitle("🎯 Guess The Number Game 🎯");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(135, 206, 250);
                Color color2 = new Color(72, 61, 139);
                g2d.setPaint(new GradientPaint(0,0,color1,0,getHeight(),color2));
                g2d.fillRect(0,0,getWidth(),getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20,50,20,50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10,10,10,10);

        JLabel titleLabel = new JLabel("🎯 Guess The Number!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40)); titleLabel.setForeground(Color.WHITE);
        JLabel instructionLabel = new JLabel("Select Difficulty to Start", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 24)); instructionLabel.setForeground(Color.WHITE);

        guessField = new JTextField(); guessField.setFont(new Font("Arial", Font.PLAIN, 28)); guessField.setHorizontalAlignment(JTextField.CENTER);

        guessButton = new JButton("Guess"); guessButton.setFont(new Font("Arial", Font.BOLD, 24));
        guessButton.setBackground(new Color(60,179,113)); guessButton.setForeground(Color.WHITE);

        restartButton = new JButton("Restart"); restartButton.setFont(new Font("Arial", Font.BOLD, 22));
        restartButton.setBackground(new Color(255,140,0)); restartButton.setForeground(Color.WHITE);

        quitButton = new JButton("Quit"); quitButton.setFont(new Font("Arial", Font.BOLD, 22));
        quitButton.setBackground(new Color(220,20,60)); quitButton.setForeground(Color.WHITE);

        messageLabel = new JLabel("You have 0 attempts.", SwingConstants.CENTER); messageLabel.setFont(new Font("Arial", Font.BOLD, 24)); messageLabel.setForeground(Color.YELLOW);
        hintLabel = new JLabel("Hints will appear here.", SwingConstants.CENTER); hintLabel.setFont(new Font("Arial", Font.PLAIN, 22)); hintLabel.setForeground(Color.ORANGE);
        attemptsLabel = new JLabel("Attempts: 0", SwingConstants.CENTER); attemptsLabel.setFont(new Font("Arial", Font.PLAIN, 20)); attemptsLabel.setForeground(Color.WHITE);
        scoreboardLabel = new JLabel("", SwingConstants.CENTER); scoreboardLabel.setFont(new Font("Arial", Font.BOLD, 20)); scoreboardLabel.setForeground(Color.MAGENTA);

        attemptsBar = new JProgressBar(0,10); attemptsBar.setValue(0); attemptsBar.setStringPainted(true); attemptsBar.setFont(new Font("Arial",Font.BOLD,18)); attemptsBar.setForeground(Color.GREEN);

        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; mainPanel.add(titleLabel,gbc); gbc.gridy++;
        mainPanel.add(instructionLabel,gbc); gbc.gridy++;
        mainPanel.add(guessField,gbc); gbc.gridy++;
        mainPanel.add(guessButton,gbc); gbc.gridy++;
        mainPanel.add(messageLabel,gbc); gbc.gridy++;
        mainPanel.add(hintLabel,gbc); gbc.gridy++;
        mainPanel.add(attemptsLabel,gbc); gbc.gridy++;
        mainPanel.add(attemptsBar,gbc); gbc.gridy++;
        mainPanel.add(scoreboardLabel,gbc);

        JPanel bottomPanel = new JPanel(); bottomPanel.setBackground(new Color(0,0,0,0));
        bottomPanel.add(restartButton); bottomPanel.add(quitButton);

        add(mainPanel, BorderLayout.CENTER); add(bottomPanel, BorderLayout.SOUTH);

        guessButton.addActionListener(e -> handleGuess());
        restartButton.addActionListener(e -> selectDifficulty());
        quitButton.addActionListener(e -> System.exit(0));

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = getWidth();
                titleLabel.setFont(new Font("Arial",Font.BOLD,w/20));
                instructionLabel.setFont(new Font("Arial",Font.BOLD,w/35));
                guessField.setFont(new Font("Arial",Font.PLAIN,w/30));
                guessButton.setFont(new Font("Arial",Font.BOLD,w/40));
                restartButton.setFont(new Font("Arial",Font.BOLD,w/45));
                quitButton.setFont(new Font("Arial",Font.BOLD,w/45));
                messageLabel.setFont(new Font("Arial",Font.BOLD,w/35));
                hintLabel.setFont(new Font("Arial",Font.PLAIN,w/40));
                attemptsLabel.setFont(new Font("Arial",Font.PLAIN,w/45));
                attemptsBar.setFont(new Font("Arial",Font.BOLD,w/50));
                scoreboardLabel.setFont(new Font("Arial",Font.BOLD,w/45));
            }
        });

        selectDifficulty();
    }

    private void selectDifficulty() {
        String[] options = {"Easy (1-30)","Medium (1-50)","Hard (1-100)"};
        int choice = JOptionPane.showOptionDialog(this,"Choose Difficulty Level:","Difficulty",
                JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,options,options[0]);
        if(choice==0){ lowerBound=1; upperBound=30; maxAttempts=10;}
        else if(choice==1){ lowerBound=1; upperBound=50; maxAttempts=10;}
        else if(choice==2){ lowerBound=1; upperBound=100; maxAttempts=10;}
        else System.exit(0);
        startNewGame();
    }

    private void startNewGame() {
        numberToGuess = rand.nextInt(upperBound-lowerBound+1)+lowerBound;
        attempts = 0; guessField.setText(""); guessField.setEditable(true); guessButton.setEnabled(true);
        messageLabel.setText("You have "+maxAttempts+" attempts."); messageLabel.setForeground(Color.YELLOW);
        hintLabel.setText("Hints will appear here."); attemptsLabel.setText("Attempts: 0");
        attemptsBar.setMaximum(maxAttempts); attemptsBar.setValue(0);
        gamesPlayed++; updateScoreboard();
    }

    private void handleGuess() {
        try {
            int userGuess = Integer.parseInt(guessField.getText());
            if(userGuess<lowerBound||userGuess>upperBound){ messageLabel.setText("⚠️ Enter a number between "+lowerBound+" and "+upperBound+"!"); messageLabel.setForeground(Color.MAGENTA); playSound("wrong.wav"); return;}

            attempts++; attemptsLabel.setText("Attempts: "+attempts); attemptsBar.setValue(attempts);

            if(userGuess==numberToGuess){
                animateMessage("🎉 Correct! You guessed in "+attempts+" attempts!", Color.GREEN); hintLabel.setText("");
                playConfetti(); playSound("correct.wav"); wins++; if(attempts<bestAttempts) bestAttempts=attempts; saveScoreboard(); updateScoreboard(); endGame();
            } else{
                playSound("wrong.wav"); String hint=generateHint(userGuess);
                animateMessage(userGuess<numberToGuess?"Too low!":"Too high!", userGuess<numberToGuess?Color.CYAN:Color.ORANGE);
                hintLabel.setText("💡 Hint: "+hint);
            }

            if(attempts>=maxAttempts && guessField.isEditable()){
                animateMessage("💀 Game Over! The number was "+numberToGuess+".", Color.RED); hintLabel.setText(""); playSound("gameover.wav"); updateScoreboard(); endGame();
            }

            guessField.setText(""); guessField.requestFocus();
        }catch(NumberFormatException ex){ animateMessage("⚠️ Please enter a valid number!", Color.MAGENTA); playSound("wrong.wav");}
    }

    private String generateHint(int guess){
        String[] hints = new String[3];
        hints[0] = numberToGuess%2==0?"The number is even.":"The number is odd.";
        int range = Math.max(1,(upperBound-lowerBound)/4);
        int low=Math.max(lowerBound, numberToGuess-rand.nextInt(range)-1);
        int high=Math.min(upperBound, numberToGuess+rand.nextInt(range)+1);
        hints[1] = "The number is between "+low+" and "+high+".";
        hints[2] = guess<numberToGuess?"Try a higher number.":"Try a lower number.";
        return hints[rand.nextInt(hints.length)];
    }

    private void updateScoreboard(){
        scoreboardLabel.setText("Games: "+gamesPlayed+" | Wins: "+wins+" | Best Attempts: "+(bestAttempts==Integer.MAX_VALUE?"-":bestAttempts));
    }

    private void saveScoreboard(){
        try(PrintWriter pw=new PrintWriter(new FileWriter(SCORE_FILE))){
            pw.println(gamesPlayed); pw.println(wins); pw.println(bestAttempts);
        }catch(Exception e){}
    }

    private void loadScoreboard(){
        try(BufferedReader br=new BufferedReader(new FileReader(SCORE_FILE))){
            gamesPlayed=Integer.parseInt(br.readLine());
            wins=Integer.parseInt(br.readLine());
            bestAttempts=Integer.parseInt(br.readLine());
        }catch(Exception e){gamesPlayed=0; wins=0; bestAttempts=Integer.MAX_VALUE;}
    }

    private void endGame(){ guessField.setEditable(false); guessButton.setEnabled(false);}

    private void animateMessage(String text, Color color){
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            int count=0;
            public void run(){
                if(count>=6){ messageLabel.setText(text); messageLabel.setForeground(color); timer.cancel();}
                else{ messageLabel.setText(text); messageLabel.setForeground(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))); count++; }
            }
        };
        timer.scheduleAtFixedRate(task,0,100);
    }

    private void playConfetti(){
        JFrame confettiFrame=new JFrame();
        confettiFrame.setSize(getWidth(), getHeight());
        confettiFrame.setUndecorated(true);
        confettiFrame.setLocationRelativeTo(this);
        confettiFrame.setBackground(new Color(0,0,0,0));
        confettiFrame.setLayout(null);
        confettiFrame.setVisible(true);
        Timer confettiTimer=new Timer();
        confettiTimer.scheduleAtFixedRate(new TimerTask(){
            int count=0;
            public void run(){
                if(count++>50){ confettiFrame.dispose(); confettiTimer.cancel();}
                else{ JLabel piece=new JLabel("🎊"); piece.setFont(new Font("Arial",Font.BOLD,24+rand.nextInt(12)));
                    piece.setBounds(rand.nextInt(getWidth()),0,30,30); confettiFrame.add(piece); piece.setVisible(true);
                    new Timer().schedule(new TimerTask(){ public void run(){ confettiFrame.remove(piece); }}, 1000);
                }
            }
        },0,100);
    }

    private void playSound(String fileName){
        try{ Clip clip = AudioSystem.getClip(); clip.open(AudioSystem.getAudioInputStream(new File(fileName))); clip.start();}
        catch(Exception e){}
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new GuessTheNumberGUI().setVisible(true));
    }
}
