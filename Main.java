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

    ArrayList projectile_List = new ArrayList(); // ź ������ �迭
    // ArrayList enemy_List = new enemy_List();
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

    private int getImageSize(Image i) { // �̹��� ������ �ҷ�����
        return i.getWidth(null);
    }

    private void init() {
        p_x = f_width / 2;
        p_y = 550;
        projectile_speed = 10;
        reload = 0;
        p_speed = 10;
    }

    private void start() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ������ �� XŰ�� ��������

        addKeyListener(this);
        th = new Thread(this); // ������ ����
        th.start(); // ������ ���� - run �Լ� ����
    }

    public void run() { // ������ ���ѹݺ� ����
        try {
            while (true) {
                KeyProcess_Move(); // Ű���� �Է�ó��
                projectileProcess(); // ź ó��
                reloading();// �߻� �� ������ ó��
                repaint(); // ���ŵ� p_x,p_y������ ���� �׸���
                Thread.sleep(20); // 20�и������� ������ �ݺ�
            }
        } catch (Exception e) {
        }
    }

    public void projectileProcess() { // Ű �Է½� ź ó�� �޼ҵ�
        if (keySpace == true) {
            if (reload <= 0) {
                pj = new Projectile(p_x, p_y);
                projectile_List.add(pj);
                Sound("fire.wav", false);
                reload = 5;
            }
        }
    }

    public void reloading() {
        reload = reload - 1;
    }

    public void paint(Graphics g) { // ���۸� ����Ͽ� ȭ�鿡 ���
        buffImage = createImage(f_width, f_height);
        buffg = buffImage.getGraphics();

        update(g);
    }

    public void update(Graphics g) {
        draw_char();
        draw_Projectile();

        g.drawImage(buffImage, 0, 0, this);
    }

    public void draw_char() { // ĳ���� �׸��� �Լ�
        int imgsize = getImageSize(player_Image);
        // buffg.clearRect(0, 0, f_width, f_height);
        buffg.drawImage(player_Image, (int) (p_x - imgsize / 2), p_y, this);
    }

    public void draw_Projectile() { // ź �׸��� �޼ҵ�
        int imgsize = getImageSize(projectile_img);

        for (int i = 0; i < projectile_List.size(); i++) {

            pj = (Projectile) (projectile_List.get(i));
            if (i % 2 == 0) {
                projectile_img = tk.getImage("ammo2.png");
            } else {
                projectile_img = tk.getImage("ammo.png");
            }
            buffg.drawImage(projectile_img, (int) (pj.pos.x - imgsize / 2), pj.pos.y - 12, this);

            pj.move(projectile_speed);// �׷��� źȯ���� ������ ���ڸ�ŭ �̵���Ű��

            if (pj.pos.x > f_width) { // ȭ�� ������ ������ ����
                projectile_List.remove(i);
            }
        }
    }

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
    }

    public void Sound(String file, boolean Loop) {
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

class Projectile { // ź ��ġ �ľ� �� �̵��� Ŭ����
    Point pos; // ź ��ǥ

    Projectile(int x, int y) {
        pos = new Point(x, y);
    }

    public void move(int projec_speed) {
        pos.y = pos.y - projec_speed;
    }
}

class Enemy {

}