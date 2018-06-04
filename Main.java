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

    Thread th; // 스레드 생성

    boolean KeyW = false; // 입력용 변수
    boolean KeyA = false;
    boolean KeyS = false;
    boolean KeyD = false;
    boolean keySpace = false;

    int p_speed; // 플레이어 캐릭터 속도
    int projectile_speed; // 탄속

    Toolkit tk = Toolkit.getDefaultToolkit(); // 이미지 불러오기용 툴킷
    Image player_Image = tk.getImage("player.png");
    Image projectile_img = tk.getImage("ammo.png");
    Image enemy_img = tk.getImage("enemy.png");

    ArrayList projectile_List = new ArrayList(); // 탄 관리용 배열
    // ArrayList enemy_List = new enemy_List();
    // ArrayList enem_projectile_List = new enem_projectile_List();

    Image buffImage;
    Graphics buffg;

    Projectile pj; // 프로젝타일 클래스 접근
    Enemy en; // 적 클래스 접근

    game_frame() {
        init();
        start();

        setTitle("JShooting");
        setSize(f_width, f_height);

        Dimension screen = tk.getScreenSize();
        int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
        int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);
        setLocation(f_xpos, f_ypos);
        setResizable(false); // 프레임 크기 조절 불가능하게
        setVisible(true);
    }

    private int getImageSize(Image i) { // 이미지 사이즈 불러오기
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 오른쪽 위 X키로 정상종료

        addKeyListener(this);
        th = new Thread(this); // 스레드 생성
        th.start(); // 스레드 실행 - run 함수 실행
    }

    public void run() { // 스레드 무한반복 구문
        try {
            while (true) {
                KeyProcess_Move(); // 키보드 입력처리
                projectileProcess(); // 탄 처리
                reloading();// 발사 후 재장전 처리
                repaint(); // 갱신된 p_x,p_y값으로 새로 그리기
                Thread.sleep(20); // 20밀리섹마다 스레드 반복
            }
        } catch (Exception e) {
        }
    }

    public void projectileProcess() { // 키 입력시 탄 처리 메소드
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

    public void paint(Graphics g) { // 버퍼를 사용하여 화면에 출력
        buffImage = createImage(f_width, f_height);
        buffg = buffImage.getGraphics();

        update(g);
    }

    public void update(Graphics g) {
        draw_char();
        draw_Projectile();

        g.drawImage(buffImage, 0, 0, this);
    }

    public void draw_char() { // 캐릭터 그리는 함수
        int imgsize = getImageSize(player_Image);
        // buffg.clearRect(0, 0, f_width, f_height);
        buffg.drawImage(player_Image, (int) (p_x - imgsize / 2), p_y, this);
    }

    public void draw_Projectile() { // 탄 그리는 메소드
        int imgsize = getImageSize(projectile_img);

        for (int i = 0; i < projectile_List.size(); i++) {

            pj = (Projectile) (projectile_List.get(i));
            if (i % 2 == 0) {
                projectile_img = tk.getImage("ammo2.png");
            } else {
                projectile_img = tk.getImage("ammo.png");
            }
            buffg.drawImage(projectile_img, (int) (pj.pos.x - imgsize / 2), pj.pos.y - 12, this);

            pj.move(projectile_speed);// 그려진 탄환들을 정해진 숫자만큼 이동시키기

            if (pj.pos.x > f_width) { // 화면 밖으로 나가면 지움
                projectile_List.remove(i);
            }
        }
    }

    public void keyPressed(KeyEvent e) { // 키보드가 눌려졌을때의 이벤트 처리 함수
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

    public void keyReleased(KeyEvent e) { // 키보드가 때어졌을때의 이벤트 처리 함수
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

    public void KeyProcess_Move() { // 키 입력값을 바탕으로 플레이 구현
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

class Projectile { // 탄 위치 파악 및 이동용 클래스
    Point pos; // 탄 좌표

    Projectile(int x, int y) {
        pos = new Point(x, y);
    }

    public void move(int projec_speed) {
        pos.y = pos.y - projec_speed;
    }
}

class Enemy {

}