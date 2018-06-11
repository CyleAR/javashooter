
// Copyleft 2018 황정환 All codes can be copied without permission

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

    int p_x; // 플레이어 x좌표
    int p_y; // 플레이어 y좌표
    int reload; // 재장전 구현용
    int reload_sec; // 재장전 간격
    int en_cnt; // 적 재장전, 재출현 구현용 카운트
    int en_width;
    int en_height;
    int pj_width;
    int pj_height;
    int score; // 점수
    int life; // 생명
    int difficulty;// 난도

    Thread th; // 스레드 생성

    boolean KeyW = false; // 입력용 변수
    boolean KeyA = false;
    boolean KeyS = false;
    boolean KeyD = false;
    boolean keySpace = false;
    boolean keyShift = false;
    boolean KeyQ = false; // 치트

    int p_speed; // 플레이어 캐릭터 속도
    int projectile_speed; // 탄속

    Toolkit tk = Toolkit.getDefaultToolkit(); // 이미지 불러오기용 툴킷
    Image player_Image = tk.getImage("player.png");
    Image projectile_img = tk.getImage("ammo.png");
    Image enemy_img = tk.getImage("enemy.png");
    Image bg_img = tk.getImage("background.png");
    Image enemy_projectile_img = tk.getImage("enemyammo.png");

    ArrayList projectile_List = new ArrayList(); // 탄 관리용 배열
    ArrayList enemy_List = new ArrayList(); // 적 관리용 배열
    ArrayList enem_projectile_List = new ArrayList(); // 적들이 쏜 탄 관리용 배열

    Image buffImage;
    Graphics buffg;

    Projectile pj; // 프로젝타일 클래스 접근
    Enemy en; // 적 클래스 접근
    EnemyProjectile enp; // 적 탄 클래스 접근

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

    private int getImageWidth(Image i) { // 이미지 너비 불러오기
        return i.getWidth(null);
    }

    private int getImageHeight(Image i) {
        return i.getHeight(null); // 이미지 높이 불러오기
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
        // Sound("Victory.wav", true);
    }

    private void start() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 오른쪽 위 X키로 정상종료
        addKeyListener(this);
        th = new Thread(this); // 스레드 생성
        th.start(); // 스레드 실행 - run 함수 실행
    }

    public void run() { // 스레드 실행되면 호출됨, 스레드 무한반복 구문
        try {
            while (true) {
                KeyProcess_Move(); // 키보드 입력처리
                projectileProcess(); // 탄 처리
                enemyProcess(); // 적 움직임 처리
                enemyProjectileProcess(); // 적이 쏜 탄 처리
                isKill(); // 적-플레이어가 쏜 탄환 간 충돌 확인
                iskilled(); // 적이쏜 탄환-플레이어 간 충돌 확인
                en_cnt++;
                reload--;
                repaint(); // 갱신된 p_x,p_y값으로 새로 그리기
                Thread.sleep(10); // 10밀리섹마다 스레드 반복 (1000ms = 1초)
            }
        } catch (Exception e) {
        }
    }

    public void projectileProcess() { // 키 입력시 탄 처리 메소드
        int imgsize = getImageWidth(projectile_img);
        if (keySpace == true) {
            if (reload <= 0) {
                pj = new Projectile((int) (p_x - imgsize / 2), p_y - 10);
                projectile_List.add(pj);
                Sound("fire.wav", false);
                reload = reload_sec; // 재장전
            }
        }
    }

    public void enemyProcess() { // 적 생성 처리
        if (en_cnt % 300 == 15 && enemy_List.size() == 0) {
            for (int i = 0; i < 10; i++) {
                en = new Enemy(100 + i * 100, 620 - 480, difficulty);
                enemy_List.add(en);
            }
            difficulty++;
        }
    }

    public void enemyProjectileProcess() { // 적이 쏜 탄 처리
        if (en_cnt % (300 - 30 * difficulty) == 0) {
            for (int i = 0; i < enemy_List.size(); i++) {
                en = (Enemy) (enemy_List.get(i));
                enp = new EnemyProjectile(en.x + 32, en.y + 50, difficulty);
                enem_projectile_List.add(enp);
            }
        }
    }

    public void isKill() { // 적이 내가 쏜 탄에 맞았나 처리
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

    public void iskilled() { // 내가 적이 쏜 탄에 맞았나 처리
        int i;
        for (i = 0; i < enem_projectile_List.size(); ++i) {
            enp = (EnemyProjectile) enem_projectile_List.get(i);
            if (isCrash(enp.x, enp.y, p_x - player_Image.getWidth(null) / 2, p_y, enemy_projectile_img, player_Image)) {
                enem_projectile_List.remove(i);
                life--;
            }
        }
    }

    public boolean isCrash(int x1, int y1, int x2, int y2, Image img1, Image img2) { // 이미지 충돌 확인
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
    public void paint(Graphics g) { // 버퍼를 사용하여 화면에 출력
        buffImage = createImage(f_width, f_height);
        buffg = buffImage.getGraphics();
        update(g);
    }

    public void update(Graphics g) { // 버퍼 업뎃
        draw_BG();
        draw_Enemy();
        draw_Projectile();
        draw_char();
        draw_EnemyProjectile();
        draw_Status();
        draw_Board();

        g.drawImage(buffImage, 0, 0, this);
    }

    public void draw_BG() { // 배경 그림
        buffg.clearRect(0, 0, f_width, f_height);
        buffg.drawImage(bg_img, 0, 0, this);
    }

    public void draw_char() { // 캐릭터 그리는 메소드
        int imgsize = getImageWidth(player_Image);
        buffg.drawImage(player_Image, (int) (p_x - imgsize / 2), p_y, this);
    }

    public void draw_Projectile() { // 탄 그리는 메소드
        for (int i = 0; i < projectile_List.size(); i++) {
            pj = (Projectile) (projectile_List.get(i));
            pj.move(projectile_speed);// 그려진 탄환들을 정해진 숫자만큼 이동시키기
            if (pj.y < 0) { // 화면 밖으로 나가면 지움
                projectile_List.remove(i);
            }
            buffg.drawImage(projectile_img, pj.x, pj.y, this);
        }
    }

    public void draw_Enemy() { // 적 이미지를 그리는 메소드
        for (int i = 0; i < enemy_List.size(); i++) {
            en = (Enemy) (enemy_List.get(i));
            en.move();
            buffg.drawImage(enemy_img, en.x, en.y, this);// 배열에 생성된 각 적을 판별하여 이미지 그리기
        }
    }

    public void draw_EnemyProjectile() { // 적이 쏜 탄 그리는 메소드
        for (int i = 0; i < enem_projectile_List.size(); i++) {
            enp = (EnemyProjectile) (enem_projectile_List.get(i));
            enp.move();
            if (enp.y > 720) { // 화면 밖으로 나가면 지움
                enem_projectile_List.remove(i);
            }
            buffg.drawImage(enemy_projectile_img, enp.x, enp.y, this);
        }
    }

    public void draw_Status() { // 스테이터스 창 그리는 메소드
        String n;
        n = "SCORE : " + score;
        buffg.setColor(Color.white);
        buffg.setFont(new Font("default", Font.BOLD, 20));
        buffg.drawString(n, 640 - 50, 70);
        buffg.drawString("Life : " + life, 640 - 50, 100);
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
        }
    }
    // 키보드 입력

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
        case KeyEvent.VK_SHIFT:
            keyShift = true;
            break;
        case KeyEvent.VK_Q:
            KeyQ = true;
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

    public void Sound(String file, boolean Loop) { // 소리 출력용 메소드
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
// 다른 클래스들//
/////////////////

class Projectile { // 탄 위치 파악 및 이동용 클래스
    int x = 0; // 탄 좌표
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