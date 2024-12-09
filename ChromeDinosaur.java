import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 750;
    int boardHeight = 250;

    // Load images
    Image dinosaurImg;
    Image dinosaurDeadImg;
    Image dinosaurJumpImg;
    Image cactus1Img;
    Image cactus2Img;
    Image cactus3Img;
    Image trackImg;  // Track image for the path
    Image birdImg;   // Bird image
    Image cloudImg;  // Cloud image

    // Dinosaur and other variables
    int dinosaurWidth = 88;
    int dinosaurHeight = 94;
    int dinosaurX = 50;
    int dinosaurY = boardHeight - dinosaurHeight;

    Block dinosaur;
    ArrayList<Block> cactusArray;

    int velocityX = -12;
    int velocityY = 0;
    int gravity = 1;

    boolean gameOver = false;
    int score = 0;
    int highScore = 0;  // Variable to store the high score
    int previousScore = 0;  // Variable to store the previous score

    Timer gameLoop;
    Timer placeCactusTimer;

    JButton retryButton;
    JLabel highScoreLabel;
    JLabel previousScoreLabel;

    // Bird and cloud movement variables
    int birdX = boardWidth;  // Starting position of the bird at the right side
    int birdY = 50; // Position of the bird vertically (at the top)
    int cloudX = boardWidth; // Starting position of the cloud at the right side
    int cloudY = 100; // Position of the cloud vertically

    public ChromeDinosaur() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.lightGray);
        setFocusable(true);
        addKeyListener(this);

        // Load images including the track, bird, and cloud
        dinosaurImg = new ImageIcon(getClass().getResource("./img/dino-run.gif")).getImage();
        dinosaurDeadImg = new ImageIcon(getClass().getResource("./img/dino-dead.png")).getImage();
        dinosaurJumpImg = new ImageIcon(getClass().getResource("./img/dino-jump.png")).getImage();
        cactus1Img = new ImageIcon(getClass().getResource("./img/cactus1.png")).getImage();
        cactus2Img = new ImageIcon(getClass().getResource("./img/cactus2.png")).getImage();
        cactus3Img = new ImageIcon(getClass().getResource("./img/cactus3.png")).getImage();
        trackImg = new ImageIcon(getClass().getResource("./img/track.png")).getImage();  // Load track image
        birdImg = new ImageIcon(getClass().getResource("./img/bird.gif")).getImage();   // Load bird image
        cloudImg = new ImageIcon(getClass().getResource("./img/cloud.png")).getImage();  // Load cloud image

        // Initialize dinosaur
        dinosaur = new Block(dinosaurX, dinosaurY, dinosaurWidth, dinosaurHeight, dinosaurImg);
        cactusArray = new ArrayList<>();

        // Game Timer
        gameLoop = new Timer(1000 / 60, this);  // 60 FPS
        gameLoop.start();

        // Place Cactus Timer
        placeCactusTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeCactus();
            }
        });
        placeCactusTimer.start();

        // Retry Button with reset.png
        retryButton = new JButton();
        ImageIcon resetIcon = new ImageIcon(getClass().getResource("./img/reset.png"));
        retryButton.setIcon(resetIcon);
        retryButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 - 50, 76, 68);  // Adjust size and position
        retryButton.setVisible(false);  // Initially hidden until the game is over
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });
        this.setLayout(null);
        this.add(retryButton);

        // High Score Label (top-right corner)
        highScoreLabel = new JLabel("High Score: " + highScore);
        highScoreLabel.setBounds(boardWidth - 210, 10, 200, 40);
        highScoreLabel.setFont(new Font("Courier", Font.PLAIN, 20));
        highScoreLabel.setForeground(Color.BLACK);
        this.add(highScoreLabel);

        // Previous Score Label (below the high score)
        previousScoreLabel = new JLabel("Previous Score: " + previousScore);
        previousScoreLabel.setBounds(boardWidth - 210, 50, 200, 40);  // Positioned below high score
        previousScoreLabel.setFont(new Font("Courier", Font.PLAIN, 20));
        previousScoreLabel.setForeground(Color.BLACK);
        this.add(previousScoreLabel);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw the track at the bottom of the frame, it stays fixed
        g.drawImage(trackImg, 0, boardHeight - trackImg.getHeight(null), boardWidth, trackImg.getHeight(null), null);

        // Draw the bird at the top (moving from right to left)
        g.drawImage(birdImg, birdX, birdY, birdImg.getWidth(null), birdImg.getHeight(null), null);

        // Draw the cloud at the top (moving from right to left)
        g.drawImage(cloudImg, cloudX, cloudY, cloudImg.getWidth(null), cloudImg.getHeight(null), null);

        // Draw dinosaur
        g.drawImage(dinosaur.img, dinosaur.x, dinosaur.y, dinosaur.width, dinosaur.height, null);

        // Draw cactus
        for (int i = 0; i < cactusArray.size(); i++) {
            Block cactus = cactusArray.get(i);
            g.drawImage(cactus.img, cactus.x, cactus.y, cactus.width, cactus.height, null);
        }

        // Draw score
        g.setColor(Color.black);
        g.setFont(new Font("Courier", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), 10, 35);
        } else {
            g.drawString(String.valueOf(score), 10, 35);
        }
    }

    void placeCactus() {
        if (gameOver) {
            return;
        }

        double placeCactusChance = Math.random();  // Random chance for cactus
        if (placeCactusChance > .90) {
            Block cactus = new Block(700, boardHeight - 70, 102, 70, cactus3Img);
            cactusArray.add(cactus);
        } else if (placeCactusChance > .70) {
            Block cactus = new Block(700, boardHeight - 70, 69, 70, cactus2Img);
            cactusArray.add(cactus);
        } else if (placeCactusChance > .50) {
            Block cactus = new Block(700, boardHeight - 70, 34, 70, cactus1Img);
            cactusArray.add(cactus);
        }

        if (cactusArray.size() > 10) {
            cactusArray.remove(0);  // Remove old cactus
        }
    }

    public void move() {
        // Dinosaur movement
        velocityY += gravity;
        dinosaur.y += velocityY;

        if (dinosaur.y > dinosaurY) {  // Stop the dinosaur from falling
            dinosaur.y = dinosaurY;
            velocityY = 0;
            dinosaur.img = dinosaurImg;
        }

        // Cactus movement
        for (int i = 0; i < cactusArray.size(); i++) {
            Block cactus = cactusArray.get(i);
            cactus.x += velocityX;

            if (collision(dinosaur, cactus)) {
                gameOver = true;
                dinosaur.img = dinosaurDeadImg;
            }
        }

        // Update score
        score++;
        if (score > highScore) {
            highScore = score;  // Update high score
            highScoreLabel.setText("High Score: " + highScore);  // Update high score label
        }

        // Move the bird and cloud from right to left
        birdX -= 2;  // Adjust the speed of the bird (negative value moves it to the left)
        if (birdX + birdImg.getWidth(null) < 0) {
            birdX = boardWidth;  // Reset bird position to the right side when it moves off-screen
        }

        cloudX -= 1;  // Adjust the speed of the cloud (negative value moves it to the left)
        if (cloudX + cloudImg.getWidth(null) < 0) {
            cloudX = boardWidth;  // Reset cloud position to the right side when it moves off-screen
        }
    }

    boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    public void restartGame() {
        // Store the current score as the previous score before resetting
        previousScore = score;
        previousScoreLabel.setText("Previous Score: " + previousScore);  // Update previous score label

        // Reset game state
        dinosaur.y = dinosaurY;
        dinosaur.img = dinosaurImg;
        velocityY = 0;
        cactusArray.clear();
        score = 0;
        gameOver = false;
        gameLoop.start();
        placeCactusTimer.start();
        retryButton.setVisible(false);  // Hide retry button after restart
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placeCactusTimer.stop();
            gameLoop.stop();
            retryButton.setVisible(true);  // Show retry button when game is over
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (dinosaur.y == dinosaurY) {
                velocityY = -17;
                dinosaur.img = dinosaurJumpImg;
            }

            if (gameOver) {
                restartGame();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    // Block class to represent the dinosaur and cactuses
    class Block {
        int x, y, width, height;
        Image img;

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chrome Dinosaur Game");
        ChromeDinosaur gamePanel = new ChromeDinosaur();
        frame.add(gamePanel);
        frame.setSize(gamePanel.boardWidth, gamePanel.boardHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
