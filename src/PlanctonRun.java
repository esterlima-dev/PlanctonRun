import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;

public class PlanctonRun extends JPanel implements ActionListener, KeyListener {

    int LarguraBorda = 360;
    int AlturaBorda = 640;

    int algasScore = 0;
    int itensScore = 0;

   private Font customFont;  

    // identificar o nome do item com base na sua imagem
    private String getItemName(Item item) {
        return item.Name;
    }

    // Itens
    class Item {
        int x, y, width, height;
        Image img;
        boolean collected = false;
        String Name;

        public Item(String Name, Image img, int x, int y) {
            this.Name = Name;
            this.img = img.getScaledInstance(72, 72, Image.SCALE_SMOOTH);
            this.x = x;
            this.y = y;
            this.width = 72;
            this.height = 72;
        }

        public void draw(Graphics g) {
            if (!collected) {
                g.drawImage(img, x, y, width, height, null);
            }
        }               
    }

    // Imagens dos itens colecionáveis
    Image alfaceImage;
    Image queijoImage;
    Image carneImage;
    Image paoImage;

    // HashMap para contar os itens coletados
    private HashMap<String, Integer> ItemCounts = new HashMap<>();
    private HashMap<String, Integer> itemScores = new HashMap<>();

    // Lista para armazenar os itens
    ArrayList<Item> itens = new ArrayList<>();

    // Imagens Principais
    Image planctonImage;
    Image backgroundImage;
    Image bottomAlgaImage;
    Image topAlgaImage;

    // Plancton
    int planctonX = LarguraBorda / 8;
    int planctonY = AlturaBorda / 2;
    int planctonWidth = 100;
    int planctonHeight = 98;

    class Plancton {
        int x = planctonX;
        int y = planctonY;
        int width = planctonWidth;
        int height = planctonHeight;
        Image img;

        Plancton(Image img) {
            this.img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    // Algas
    int AlgaX = LarguraBorda;
    int AlgaY = 0;
    int AlgaWidth = 64;
    int AlgaHeight = 512;

    class Alga {
        int x = AlgaX;
        int y = AlgaY;
        int width = AlgaWidth;
        int height = AlgaHeight;
        Image img;
        boolean Passed = false;

        Alga(Image img) {
            this.img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    // Lógica do jogo
    Plancton plancton;
    int VelocityX = -9;
    int VelocityY = 0;
    int gravity = 1;
    ArrayList<Alga> algas;
    Random random = new Random();
    Timer gameLoop;
    Timer PlaceAlgasTimer;
    boolean gameOver = false;
    double counter = 0;

    PlanctonRun() {

        try {

            File fontFile = new File("src/FONT/SpongeboyRegular-gx2n6.otf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            customFont = font.deriveFont(24f);
        } catch (Exception e){
            e.printStackTrace();
            customFont = new Font("Arial", Font.PLAIN,30);

            setPreferredSize(new Dimension(360, 640));
        setFocusable(true);
        addKeyListener(this);
        }


        setPreferredSize(new Dimension(LarguraBorda, AlturaBorda));
        setFocusable(true);
        addKeyListener(this);

        // Pontuações de cada item
        itemScores.put("Alface", 5);  // Alface vale 5 pontos
        itemScores.put("Queijo", 10); // Queijo vale 10 pontos
        itemScores.put("Carne", 15);  // Carne vale 15 pontos
        itemScores.put("Pão", 20);    // Pão vale 20 pontos


        backgroundImage = new ImageIcon(getClass().getResource("./background.png")).getImage();
        planctonImage = new ImageIcon(getClass().getResource("./plancton.png")).getImage();
        topAlgaImage = new ImageIcon(getClass().getResource("./algatopimage.png")).getImage();
        bottomAlgaImage = new ImageIcon(getClass().getResource("./algabottomimage.png")).getImage();
        alfaceImage = new ImageIcon(getClass().getResource("./alface.png")).getImage();
        queijoImage = new ImageIcon(getClass().getResource("./queijo.png")).getImage();
        carneImage = new ImageIcon(getClass().getResource("./hamburguer.png")).getImage();
        paoImage = new ImageIcon(getClass().getResource("./pao.png")).getImage();

        // Iniciar objetos
        plancton = new Plancton(planctonImage);
        algas = new ArrayList<>();
        itens = new ArrayList<>();

        // Configurar timers
        PlaceAlgasTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaceAlgas();
                PlaceItens();
            }
        });

        PlaceAlgasTimer.start();
        gameLoop = new Timer(500 / 60, this);
        gameLoop.start();
    }

    private int getItemScore(Item item){
        if (item.img == alfaceImage){
            return 5; // Alface vale 5 pontos 
        } else if (item.img == queijoImage) {
            return 10; // queijo vale 10 pontos
        } else if (item.img == carneImage) {
            return 15; // carne vale 15 pontos
        } else if (item.img == paoImage) {
            return 20; // pao vale 20 pontos 
        }
        return 0;

    }

    public void PlaceItens() {
        int ItemY = random.nextInt(AlturaBorda - 100);
        int ItemX = LarguraBorda + random.nextInt(200);  // deixa os itens apareçam dentro da tela

        Image itemImage = null;
        String itemName = "";
        int itemType = random.nextInt(4);

        switch (itemType) {
            case 0:
                itemImage = alfaceImage;
                itemName = "Alface";
                break;
            case 1:
                itemImage = queijoImage;
                itemName = "Queijo";
                break;
            case 2:
                itemImage = carneImage;
                itemName = "Carne";
                break;
            default:
                itemImage = paoImage;
                itemName = "Pão";
                break;
        }
        
        itens.add(new Item(itemName, itemImage, ItemX, ItemY));           
    }

    public void PlaceAlgas() {
        int randomAlgaY = (int)(AlgaY - AlgaHeight / 3 - Math.random() * AlgaHeight /3);
        int OpeningSpace = AlturaBorda / 4;

        //alga superior
        Alga topAlga = new Alga(topAlgaImage);
        topAlga.y = randomAlgaY;
        topAlga.x = LarguraBorda;
        algas.add(topAlga);

        //alga inferior
        Alga bottomAlga = new Alga(bottomAlgaImage);
        bottomAlga.y = topAlga.y + AlgaHeight + OpeningSpace;
        bottomAlga.x = LarguraBorda;
        algas.add(bottomAlga);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, LarguraBorda, AlturaBorda, null);
        g.drawImage(plancton.img, plancton.x, plancton.y, plancton.width, plancton.height, null);        
        
        // Desenha as algas
        for (Alga alga : algas) {
            g.drawImage(alga.img, alga.x, alga.y, alga.width, alga.height, null);
        }
    
        // Desenha os itens coletáveis
        for (Item item : itens) {
            item.draw(g);  // Chama o método draw de cada item
        }
    
        // Exibe o score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String algasScoreText = "Pontuação: " + algasScore;
        g.drawString(algasScoreText, 20, 40);
    
        String itensScoreText = "Itens: " + itensScore;
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(itensScoreText);
        g.drawString(itensScoreText, LarguraBorda - textWidth - 20, 40);
    
        // Desenha os itens coletados e suas quantidades no canto inferior
        int xOffset = 20;
        int yOffset = AlturaBorda - 50;
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        for (String itemName : ItemCounts.keySet()) {
            Image itemImage = null;
            switch (itemName) {
                case "Alface":
                    itemImage = alfaceImage;
                    break;
                case "Queijo":
                    itemImage = queijoImage;
                    break;
                case "Carne":
                    itemImage = carneImage;
                    break;
                case "Pão":
                    itemImage = paoImage;
                    break;
            }
    
            if (itemImage != null) {
                itemImage = itemImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH);  // Reduz a imagem para 32x32
                g.drawImage(itemImage, xOffset, yOffset, 32, 32, null);  // Desenha a imagem
                xOffset += 40; // Espaço entre as imagens
    
                // Exibe a pontuação (quantidade de item coletado)
                String itemScoreText = "" + ItemCounts.get(itemName);  // Quantidade coletada do item
                g.drawString(itemScoreText, xOffset - 10, yOffset + 30);  // Mostra a quantidade do item ao lado da imagem
                xOffset += 50;  // Mostra a quantidade do item
            }
        }
    
        // Tela de Game Over

        if (gameOver) {
                        g.setColor(Color.WHITE);  // Cor do texto de Game Over
            g.setFont(customFont);
            
            String gameOverText = "GAME OVER";
            
            //  centralizar largura texto 
            FontMetrics fm = g.getFontMetrics();
            int textwidth = fm.stringWidth(gameOverText); 
            int xPosition = (LarguraBorda - textWidth) / 10; // 
            g.drawString(gameOverText, xPosition, AlturaBorda / 3);
    
            // Exibe a pontuação final    
            String finalScoreText = "Pontuação Final: " + itensScore;
            g.setFont(new Font("Arial", Font.BOLD, 20));
            fm = g.getFontMetrics(); // Obtém as métricas novamente (caso a fonte tenha mudado)
            textWidth = fm.stringWidth(finalScoreText); // Calcula a largura da pontuação
            xPosition = (LarguraBorda - textWidth) / 2; // Centra o texto horizontalmente
            g.drawString(finalScoreText, xPosition, AlturaBorda / 2);
    
            // Instrução para reiniciar o jogo
            String restartText = " space para reiniciar";
            g.setFont(new Font("Arial", Font.BOLD, 16));
            fm = g.getFontMetrics(); // Obtém as métricas novamente
            textWidth = fm.stringWidth(restartText); // Calcula a largura da instrução
            xPosition = (LarguraBorda - textWidth) / 2; // Centra o texto horizontalmente
            g.drawString(restartText, xPosition, AlturaBorda / 2 + 40);
        }
    }
    

    public void move() {
        VelocityY += gravity;
        plancton.y += VelocityY;
        plancton.y = Math.max(plancton.y, 0);
    
        // Itera sobre os itens
        for (int i = 0; i < itens.size(); i++) {
            Item item = itens.get(i);
            item.x += VelocityX;
    
            // Verifica se houve colisão com o Plancton e se o item ainda não foi coletado
            if (collision(plancton, item) && !item.collected) {
                item.collected = true;  
                
                // Atualiza o HashMap de contagem de itens
                String itemName = getItemName(item);
                ItemCounts.put(itemName, ItemCounts.getOrDefault(itemName, 0) + 1);
    
                // Adiciona a pontuação com base no item coletado
                int itemScore = itemScores.getOrDefault(itemName, 0);
                itensScore += itemScore;  // Atualiza a pontuação com base no tipo de item
                System.out.println("Item coletado: " + itemName + ", Pontuação: " + itemScore);
            }
        }
    
        // Lógica de colisão com as algas
        for (int i = 0; i < algas.size(); i++) {
            Alga alga = algas.get(i);
            alga.x += VelocityX;
    
            // Verifica se o Plancton passou pela alga
            if (!alga.Passed && plancton.x > alga.x + alga.width) {
                alga.Passed = true;
                algasScore += 1;  // Aumenta o score quando o Plancton passa pela alga
            }
    
            // Verifica colisão com a alga
            if (!gameOver && collision(plancton, alga)) {
                gameOver = true;
            }
        }
    
        // Verifica se o Plancton caiu
        if (plancton.y > AlturaBorda) {
            gameOver = true;
        }
    }    

    public boolean collision(Plancton a, Alga b) {
        int margem = 50;
        return a.x + margem < b.x + b.width &&
               a.x + a.width - margem > b.x &&
               a.y + margem < b.y + b.height &&
               a.y + a.height - margem > b.y;
    }

    public boolean collision(Plancton a, Item b) {
        int margem = 25;
        return a.x + margem < b.x + b.width &&
               a.x + a.width - margem > b.x &&
               a.y + margem < b.y + b.height &&
               a.y + a.height - margem > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        if (gameOver) {
            PlaceAlgasTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            VelocityY = -9;
        }

        if (gameOver) {
            plancton.y = planctonY;
            plancton.x = planctonX;
            algas.clear();
            itens.clear();  // Limpa os itens coletados
            ItemCounts.clear();  // Limpa o HashMap de itens coletados
            itensScore = 0;  // Reseta o score dos itens
            algasScore = 0; // reseta o score das algas
            gameOver = false;
            PlaceAlgasTimer.start();
            gameLoop.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}