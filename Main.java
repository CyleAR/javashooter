import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        game_frame fm = new game_frame();
    }
}

class game_frame extends JFrame implements KeyListener, Runnable {
    int f_width = 1280;
    int f_height = 720;

    int p_x;
    int p_y;
    int reload;
    int en_cnt;
    int en_width;
    int en_height;
    int pj_width;
    int pj_height;

    Thread th; // ������ ����

    boolean KeyW = false; // �Է¿� ����
    boolean KeyA = false;
    boolean KeyS = false;
    boolean KeyD = false;
    boolean keySpace = false;

    int p_speed; // �÷��̾� ĳ���� �ӵ�
    int projectile_speed; // ź��

    Toolkit tk = Toolkit.getDefaultToolkit(); // �̹��� �ҷ������ ��Ŷ
    Image player_Image = tk.getImage("player.png");
    Image projectile_img = tk.getImage("ammo.png");
    Image enemy_img = tk.getImage("enemy.png");
    Image bg_img = tk.getImage("background.png");

    ArrayList projectile_List = new ArrayList(); // ź ������ �迭
    ArrayList enemy_List = new ArrayList();
    // ArrayList enem_projectile_List = new enem_projectile_List();

    Image buffImage;
    Graphics buffg;

    Projectile pj; // ������Ÿ�� Ŭ���� ����
    Enemy en; // �� Ŭ���� ����

    game_frame() {
        init();
        start();

        setTitle("JShooting");
        setSize(f_width, f_height);

        Dimension screen = tk.getScreenSize();
        int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
        int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);
        setLocation(f_xpos, f_ypos);
        setResizable(false); // ������ ũ�� ���� �Ұ����ϰ�
        setVisible(true);
    }

    private int getImageWidth(Image i) { // �̹��� ������ �ҷ�����
        return i.getWidth(null);
    }

    private int getImageHeight(Image i) {
        return i.getHeight(null); // �̹��� ���� �ҷ�����
    }

    private void init() {
        p_x = f_width / 2;
        p_y = 580;
        projectile_speed = 10;
        reload = 0;
        p_speed = 5;
        en_cnt = 0;
        en_width = getImageWidth(enemy_img);
        en_height = getImageHeight(enemy_img);
        pj_width = getImageWidth(projectile_img);
        pj_height = getImageHeight(projectile_img);
        Sound("Victory.wav", true);
    }

    private void start() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ������ �� XŰ�� ��������
        addKeyListener(this);
        th = new Thread(this); // ������ ����
        th.start(); // ������ ���� - run �Լ� ����
    }

    public void run() { // ������ ����Ǹ� ȣ���, ������ ���ѹݺ� ����
        try {
            while (true) {
                KeyProcess_Move(); // Ű���� �Է�ó��
                projectileProcess(); // ź ó��
                enemyProcess(); // �� ������ ó��
                //isCrush();
                en_cnt = en_cnt + 1;
                reload = reload - 1;
                repaint(); // ���ŵ� p_x,p_y������ ���� �׸���
                Thread.sleep(10); // 10�и������� ������ �ݺ� (1000ms = 1��)
                
            }
        } catch (Exception e) {
        }
    }

    public void projectileProcess() { // Ű �Է½� ź ó�� �޼ҵ�
        int imgsize = getImageWidth(projectile_img);
        if (keySpace == true) {
            if (reload <= 0) {
                pj = new Projectile((int) (p_x - imgsize / 2), p_y - 10);
                projectile_List.add(pj);
                Sound("fire.wav", false);
                reload = 40; // ������
            }
        }
    }

    public void isCrush(){
        int i;
        int j;
        for (i = 0; i < projectile_List.size(); i++) {
            pj = (Projectile) projectile_List.get(i);
            for (j = 0; j < enemy_List.size(); j++) {
                en = (Enemy) enemy_List.get(j);
                if (isCrash(en.x,pj.x,en_width,pj_width)) {
                    projectile_List.remove(i);
                    enemy_List.remove(j);
                }
            }
        }
    }
    
    public boolean isCrash(int x1, int x2,int x1_,int x2_) { // �浹Ȯ��
        boolean check = false;
        if (x1 <= x2 + x2_/2 && x2+x2_ <= x1+x1_) {
            check = true;
        } else {
            check = false;
        }

        return check; // check�� ���� ����
    }

    public void enemyProcess() { // �� ó��
        for (int i = 0; i < enemy_List.size(); i++) {
            en = (Enemy) (enemy_List.get(i));
            en.move();
            if (en.x > f_width - 150) {
                enemy_List.remove(i);
            }
        }

        if (en_cnt % 100 == 0) {

            en = new Enemy(100, 620 - 300);
            enemy_List.add(en);
            en = new Enemy(100, 620 - 400);
            enemy_List.add(en);
            en = new Enemy(100, 620 - 500);
            enemy_List.add(en);
        }
    }


    public void paint(Graphics g) { // ���۸� ����Ͽ� ȭ�鿡 ���
        buffImage = createImage(f_width, f_height);
        buffg = buffImage.getGraphics();

        update(g);
    }

    public void update(Graphics g) { // ���� ����
        draw_BG();

        draw_Enemy();
        draw_Projectile();
        draw_char();

        g.drawImage(buffImage, 0, 0, this);
    }

    public void draw_BG() {
        buffg.drawImage(bg_img, 0, 0, this);
    }

    public void draw_char() { // ĳ���� �׸��� �޼ҵ�
        int imgsize = getImageWidth(player_Image);
        // buffg.clearRect(0, 0, f_width, f_height);
        buffg.drawImage(player_Image, (int) (p_x - imgsize / 2), p_y, this);
    }

    public void draw_Projectile() { // ź �׸��� �޼ҵ�
        for (int i = 0; i < projectile_List.size(); i++) {

            pj = (Projectile) (projectile_List.get(i));
            if (i % 2 == 0) {
                projectile_img = tk.getImage("ammo2.png");
            } else {
                projectile_img = tk.getImage("ammo.png");
            }
            pj.move(projectile_speed);// �׷��� źȯ���� ������ ���ڸ�ŭ �̵���Ű��
            if (pj.y > f_height) { // ȭ�� ������ ������ ����
                projectile_List.remove(i);
            }
            buffg.drawImage(projectile_img, pj.x, pj.y, this);
        }
    }

    public void draw_Enemy() { // �� �̹����� �׸��� �޼ҵ�
        for (int i = 0; i < enemy_List.size(); i++) {
            en = (Enemy) (enemy_List.get(i));
            buffg.drawImage(enemy_img, en.x, en.y, this);// �迭�� ������ �� ���� �Ǻ��Ͽ� �̹��� �׸���
        }
    }

    // Ű���� �Է�

    public void keyPressed(KeyEvent e) { // Ű���尡 ������������ �̺�Ʈ ó�� �Լ�
        switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
            KeyW = true;
            break;
        case KeyEvent.VK_A:
            KeyA = true;
            break;
        case KeyEvent.VK_S:
            KeyS = true;
            break;
        case KeyEvent.VK_D:
            KeyD = true;
            break;
        case KeyEvent.VK_SPACE:
            keySpace = true;
            break;
        default:
            break;
        }
    }

    public void keyReleased(KeyEvent e) { // Ű���尡 ������������ �̺�Ʈ ó�� �Լ�
        switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
            KeyW = false;
            break;
        case KeyEvent.VK_A:
            KeyA = false;
            break;
        case KeyEvent.VK_S:
            KeyS = false;
            break;
        case KeyEvent.VK_D:
            KeyD = false;
            break;
        case KeyEvent.VK_SPACE:
            keySpace = false;
            break;
        default:
            break;
        }
    }
    public void keyTyped(KeyEvent e){
    }

    public void KeyProcess_Move() { // Ű �Է°��� �������� �÷��� ����
        if (KeyA == true) {
            if (p_x >= 0) {
                p_x = p_x - p_speed;
            }
        }
        if (KeyD == true) {
            if (p_x <= 1280) {
                p_x = p_x + p_speed;
            }
        }
        if (KeyW == true) {
            if (p_y >= 450) {
                p_y = p_y - p_speed;
            }
        }
        if (KeyS == true) {
            if (p_y <= 680) {
                p_y = p_y + p_speed;
            }
        }
    }

    public void Sound(String file, boolean Loop) { // �Ҹ� ��¿� �޼ҵ�
        Clip clip;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
            if (Loop) {
                clip.loop(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// �ٸ� Ŭ������

class Projectile { // ź ��ġ �ľ� �� �̵��� Ŭ����
    int x = 0; // ź ��ǥ
    int y = 0;

    Projectile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int projec_speed) {
        y = y - projec_speed;
    }
}

class Enemy {
    int x;
    int y;

    Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        x = x + 1;
    }

}