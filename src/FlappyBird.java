import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

// Classe principale du jeu, héritant de JPanel pour le dessin et implémentant ActionListener et KeyListener pour la gestion du jeu et des entrées clavier.
public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Dimensions de la fenêtre de jeu
    int boardWidth = 360;
    int boardHeight = 640;

    // Images des éléments du jeu 
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Position et taille de l'oiseau 
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    // Classe interne représentant l'oiseau
    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Position et taille des tuyaux
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;     // Mise à l'echelle par 1/6
    int pipeHeight = 512;

    // Classe interne représentant les tuyaux
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false; // Vérifie si l'oiseau est passé à travers un tuyau (pour le score)

        Pipe(Image img) {
            this.img = img;  
        }
    }

    // Variables de jeu
    Bird bird;
    int velocityX = -4; // Déplacer les tuyaux vers la vitesse de gauche (simule un oiseau se déplaçant vers la droite)
    int velocityY = 0; // Déplacer l'oiseau vers le haut/bas
    int gravity = 1; // Gravité qui attire l'oiseau vers le bas

    ArrayList<Pipe> pipes; // Liste des tuyaux sur l'écran
    Random random = new Random();

    Timer gameLoop;            // Timer pour la boucle de jeu (60 FPS)
    Timer placePipesTimer;     // Timer pour générer de nouveaux tuyaux
    boolean gameOver = false;
    double score = 0;

    // Constructeur de la classe FlappyBird
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this); // Ajout du KeyListener pour gérer les touches du clavier

        // Charge les images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialisation de l'oiseau et des tuyaux
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // Timer pour générer de nouveaux tuyaux toutes les 1,5 secondes
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();
 
        // Timer principal du jeu (60 FPS)
        gameLoop = new Timer(1000/60, this); //1000/60 = 16.6
        gameLoop.start();   
    }

    public void placePipes() {
        // Calcul de la hauteur aléatoire des tuyaux
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4; // Espace entre les tuyaux pour laisser passer l'oiseau

        // Tuyau supérieur
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        // Tuyau inférieur
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    // Eléments du jeu
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Arrière-plan du jeu
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Oiseau
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Tuyaux
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score 
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    // Déplacement de l'oiseau et des tuyaux
    public void move() {
        
        // Mouvement de l'oiseau avec la gravité  
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Empêche l'oiseau de sortir par le haut de l'écran

        // Mouvement des tuyaux
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;
            
            // Vérification du passage à travers les tuyaux (incrémentation du score)
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // 0,5 car il y a 2 tuyaux par ouverture
            }
            
            // Détection de collision
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Vérifie si l'oiseau tombe hors de l'écran
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    // Gestion des collisions entre l'oiseau et les tuyaux 
    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && // Le coin supérieur gauche de a n'atteint pas le coin supérieur droit de b
               a.x + a.width > b.x && // Le coin supérieur droit de a dépasse le coin supérieur gauche de b
               a.y < b.y + b.height && // Le coin supérieur gauche de a n'atteint pas le coin inférieur gauche de b
               a.y + a.height > b.y; // Le coin inférieur gauche de a dépasse le coin supérieur gauche de b
    }

    // Gestion de la boucle de jeu
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    // Gestion des entrées clavier (barre ESPACE pour sauter)
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9; // Fait sauter l'oiseau
            if (gameOver) {
                // Réinitialise le jeu après un Game Over
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
