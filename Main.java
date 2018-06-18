
// Copyleft 2018 Ȳ��ȯ All codes can be copied without permission

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.Math;

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
    private static final long serialVersionUID = 1L;
    int f_width = 1280;
    int f_height = 720;

    int p_x; // �÷��̾� x��ǥ
    int p_y; // �÷��̾� y��ǥ
    int reload; // ������ ������
    int reload_sec; // ������ ����
    int en_cnt; // �� ������, ������ ������ ī��Ʈ
    int en_width;
    int en_height;
    int pj_width;
    int pj_height;
    int score; // ����
    int life; // ����
    int difficulty;// ����

    Thread th; // ������ ����

    boolean KeyW = false; // �Է¿� ����
    boolean KeyA = false;
    boolean KeyS = false;
    boolean KeyD = false;
    boolean keySpace = false;
    boolean keyShift = false;
    boolean KeyQ = false; // ġƮ

    int p_speed; // �÷��̾� ĳ���� �ӵ�
    int projectile_speed; // ź��

    Toolkit tk = Toolkit.getDefaultToolkit(); // �̹��� �ҷ������ ��Ŷ
    Image player_Image = tk.getImage("player.png");
    Image projectile_img = tk.getImage("ammo.png");
    Image enemy_img = tk.getImage("enemy.png");
    Image bg_img = tk.getImage("background.png");
    Image enemy_projectile_img = tk.getImage("enemyammo.png");

    ArrayList projectile_List = new ArrayList(); // ź ������ �迭
    ArrayList enemy_List = new ArrayList(); // �� ������ �迭
    ArrayList enem_projectile_List = new ArrayList(); // ������ �� ź ������ �迭

    Image buffImage;
    Graphics buffg;

    Projectile pj; // ������Ÿ�� Ŭ���� ����
    Enemy en; // �� Ŭ���� ����
    EnemyProjectile enp; // �� ź Ŭ���� ����

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

    private int getImageWidth(Image i) { // �̹��� �ʺ� �ҷ�����
        return i.getWidth(null);
    }

    private int getImageHeight(Image i) {
        return i.getHeight(null); // �̹��� ���� �ҷ�����
    }

    private void init() {
        p_x = f_width / 2;
        p_y = 580;
        projectile_speed = 8;
        reload = 0;
        p_speed = 4;
        en_cnt = 0;
        en_width = getImageWidth(enemy_img);
        en_height = getImageHeight(enemy_img);
        pj_width = getImageWidth(projectile_img);
        pj_height = getImageHeight(projectile_img);
        score = 0;
        life = 3;
        difficulty = 1;
        reload_sec = 30;
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
                enemyProjectileProcess(); // ���� �� ź ó��
                isKill(); // ��-�÷��̾ �� źȯ �� �浹 Ȯ��
                iskilled(); // ���̽� źȯ-�÷��̾� �� �浹 Ȯ��
                en_cnt++;
                reload--;
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
                reload = reload_sec; // ������
            }
        }
    }

    public void enemyProcess() { // �� ���� ó��
        if (en_cnt % 300 == 15 && enemy_List.size() == 0) {
            for (int i = 0; i < 10; i++) {
                en = new Enemy(100 + i * 100, 620 - 480, difficulty);
                enemy_List.add(en);
            }
            difficulty++;
        }
    }

    public void enemyProjectileProcess() { // ���� �� ź ó��
        if (en_cnt % (300 - 30 * difficulty) == 0) {
            for (int i = 0; i < enemy_List.size(); i++) {
                en = (Enemy) (enemy_List.get(i));
                enp = new EnemyProjectile(en.x + 32, en.y + 50, difficulty);
                enem_projectile_List.add(enp);
            }
        }
    }

    public void isKill() { // ���� ���� �� ź�� �¾ҳ� ó��
        int i;
        int j;
        for (i = 0; i < projectile_List.size(); ++i) {
            pj = (Projectile) projectile_List.get(i);
            for (j = 0; j < enemy_List.size(); ++j) {
                en = (Enemy) enemy_List.get(j);
                if (isCrash(pj.x, pj.y, en.x, en.y, projectile_img, enemy_img)) {
                    projectile_List.remove(i);
                    enemy_List.remove(j);
                    score++;
                }
            }
        }
    }

    public void iskilled() { // ���� ���� �� ź�� �¾ҳ� ó��
        int i;
        for (i = 0; i < enem_projectile_List.size(); ++i) {
            enp = (EnemyProjectile) enem_projectile_List.get(i);
            if (isCrash(enp.x, enp.y, p_x - player_Image.getWidth(null) / 2, p_y, enemy_projectile_img, player_Image)) {
                enem_projectile_List.remove(i);
                life--;
            }
        }
    }

    public boolean isCrash(int x1, int y1, int x2, int y2, Image img1, Image img2) { // �̹��� �浹 Ȯ��
        boolean check = false;
        if (Math.abs((x1 + img1.getWidth(null) / 2) - (x2 + img2.getWidth(null) / 2)) < (img2.getWidth(null) / 2
                + img1.getWidth(null) / 2)
                && Math.abs((y1 + img1.getHeight(null) / 2)
                        - (y2 + img2.getHeight(null) / 2)) < (img2.getHeight(null) / 2 + img1.getHeight(null) / 2)) {
            check = true;
        } else {
            check = false;
        }
        return check;
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
        draw_EnemyProjectile();
        draw_Status();
        draw_Board();

        g.drawImage(buffImage, 0, 0, this);
    }

    public void draw_BG() { // ��� �׸�
        buffg.clearRect(0, 0, f_width, f_height);
        buffg.drawImage(bg_img, 0, 0, this);
    }

    public void draw_char() { // ĳ���� �׸��� �޼ҵ�
        int imgsize = getImageWidth(player_Image);
        buffg.drawImage(player_Image, (int) (p_x - imgsize / 2), p_y, this);
    }

    public void draw_Projectile() { // ź �׸��� �޼ҵ�
        for (int i = 0; i < projectile_List.size(); i++) {
            pj = (Projectile) (projectile_List.get(i));
            pj.move(projectile_speed);// �׷��� źȯ���� ������ ���ڸ�ŭ �̵���Ű��
            if (pj.y < 0) { // ȭ�� ������ ������ ����
                projectile_List.remove(i);
            }
            buffg.drawImage(projectile_img, pj.x, pj.y, this);
        }
    }

    public void draw_Enemy() { // �� �̹����� �׸��� �޼ҵ�
        for (int i = 0; i < enemy_List.size(); i++) {
            en = (Enemy) (enemy_List.get(i));
            en.move();
            buffg.drawImage(enemy_img, en.x, en.y, this);// �迭�� ������ �� ���� �Ǻ��Ͽ� �̹��� �׸���
        }
    }

    public void draw_EnemyProjectile() { // ���� �� ź �׸��� �޼ҵ�
        for (int i = 0; i < enem_projectile_List.size(); i++) {
            enp = (EnemyProjectile) (enem_projectile_List.get(i));
            enp.move();
            if (enp.y > 720) { // ȭ�� ������ ������ ����
                enem_projectile_List.remove(i);
            }
            buffg.drawImage(enemy_projectile_img, enp.x, enp.y, this);
        }
    }

    public void draw_Status() { // �������ͽ� â �׸��� �޼ҵ�
        String n;
        n = "SCORE : " + score;
        buffg.setColor(Color.white);
        buffg.setFont(new Font("default", Font.BOLD, 20));
        buffg.drawString(n, 640 - 50, 70);
        buffg.drawString("Life : " + life + " / 3", 640 - 50, 100);
    }

    public void draw_Board() {
        if (life <= 0) {
            buffg.clearRect(0, 0, f_width, f_height);
            buffg.setColor(Color.black);
            buffg.setFont(new Font("default", Font.BOLD, 100));
            buffg.drawString("You Lose", 640 - 210, 370);
        } else if (score >= 50) {
            buffg.clearRect(0, 0, f_width, f_height);
            buffg.setColor(Color.black);
            buffg.setFont(new Font("default", Font.BOLD, 100));
            buffg.drawString("You Win!", 640 - 210, 370);
            life = 1000;
        }
    }
    // Ű���� �Է�

    public void keyPressed(KeyEvent e) { // Ű���尡 ������������ �̺�Ʈ ó�� �Լ�
        switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
            KeyW = true;
            break;
        case KeyEvent.VK_LEFT:
            KeyA = true;
            break;
        case KeyEvent.VK_S:
            KeyS = true;
            break;
        case KeyEvent.VK_RIGHT:
            KeyD = true;
            break;
        case KeyEvent.VK_SPACE:
            keySpace = true;
            break;
        case KeyEvent.VK_SHIFT:
            keyShift = true;
            break;
        case KeyEvent.VK_Q:
            //KeyQ = true;
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
        case KeyEvent.VK_LEFT:
            KeyA = false;
            break;
        case KeyEvent.VK_S:
            KeyS = false;
            break;
        case KeyEvent.VK_RIGHT:
            KeyD = false;
            break;
        case KeyEvent.VK_SPACE:
            keySpace = false;
            break;
        case KeyEvent.VK_SHIFT:
            keyShift = false;
            break;
        case KeyEvent.VK_Q:
            KeyQ = false;
            break;
        default:
            break;
        }
    }

    public void keyTyped(KeyEvent e) {
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
            if (p_y >= 470) {
                // p_y = p_y - p_speed;
            }
        }
        if (KeyS == true) {
            if (p_y <= 650) {
                // p_y = p_y + p_speed;
            }
        }
        if (KeyQ == true) {
            life = life + 1;
        }
        if (keyShift == true) {
            p_speed = 1;
            reload_sec = 10;
        } else if (keyShift == false) {
            p_speed = 8;
            reload_sec = 30;
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

/////////////////
// �ٸ� Ŭ������//
/////////////////

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
    int cnt = 0;
    boolean l_or_r = true;
    int speed;

    Enemy(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.speed = z;
    }

    public void move() {
        if (l_or_r) {
            x = x + 1;
            cnt++;
            if (cnt > 100) {
                l_or_r = false;
            }
        } else {
            x = x - 1;
            cnt--;
            if (cnt == 0) {
                l_or_r = true;
            }
        }
    }
}

class EnemyProjectile {
    int x;
    int y;
    int pj_speed;

    EnemyProjectile(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.pj_speed = z;
    }

    public void move() {
        y = y + 5 + pj_speed;
    }
}