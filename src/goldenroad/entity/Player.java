package goldenroad.entity;
    
import goldenroad.input.KeyHandler;
import goldenroad.map.CollisionMap;
import goldenroad.map.CollisionHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

public class Player extends Entity {

    // ====== SPRITE ======
    private BufferedImage spriteSheet;

    private static final int FRAME_W = 96;
    private static final int FRAME_H = 96;
    private static final int DRAW_OFFSET_X = -38; 
    private static final int DRAW_OFFSET_Y = -46;

    // ====== ANIMATON ======
    private int currentFrame = 0;
    private int animationTick = 0;
    private static final int ANIMATION_SPEED = 5;

    private enum PlayerState {
        IDLE, RUN, DASH, JUMP, FALL
    }

    private PlayerState state = PlayerState.IDLE;
    private PlayerState prevState = PlayerState.IDLE;

    private static final int IDLE_ROW = 0;
    private static final int RUN_ROW = 1;
    private static final int DASH_ROW = 2;
    private static final int JUMP_ROW = 3;
    private static final int FALL_ROW = 4;

    private static final int IDLE_FRAMES = 12;
    private static final int RUN_FRAMES = 12;
    private static final int DASH_FRAMES = 4;
    private static final int JUMP_FRAMES = 4;
    private static final int FALL_FRAMES = 4;

    // ====== MOVEMENT ======
    private double velocityY = 0;
    private double velocityX = 0;
    private boolean onGround = true;

    private double direction = 1;

    private double MOVE_SPEED = 6;
    private double GRAVITY = 1.2;
    private double FALL_GRAVITY = 0.7;
    private double JUMP_SPEED = -17;
    private double MAX_FALL = 12.0;
    
    private int jumpCount = 0;
    private static final int MAX_JUMPS = 2;
    private int dropDownTimer = 0;
    private static final int DROP_DOWN_DURATION = 10;

    // ====== DASH ======
    private double DASH_SPEED = 10;
    private int dashDuration = 0;
    private int dashCooldown = 0;
    private boolean dashUsed = false;
    private int dashOnAirCount = 0;
    private static final int MAX_DASH_ON_AIR = 1;

    private static final int DASH_DURATION = 20;
    private static final int DASH_COOLDOWN = 40;

    // ====== RESOURCES ======
    private int hp = 100;
    private int maxHp = 100;
    private int mp = 100;
    private int maxMp = 100;
    private double mpRegenAccumulator = 0;
    private static final double MP_REGEN_PER_FRAME = 5.0 / 60.0;

    // ====== COYOTE + BUFFER ======
    private int coyoteTime = 0;
    private int jumpBuffer = 0;

    private static final int COYOTE_MAX = 12;
    private static final int BUFFER_MAX = 12;

    // ====== SIZE ======
    private double WIDTH = 20;
    private double HEIGHT = 50;

    public float getX() { return x; }
    public float getY() { return y; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }

    public void setVelocityY(double v) { this.velocityY = v; }

    public void setX(double x) { this.x = (float)x; }
    public void setY(double y) { this.y = (float)y; }

    public double getWidth() { return WIDTH; }
    public double getHeight() { return HEIGHT; }

    // ====== UPDATE ======
    public void update(KeyHandler input) {
        double moveX = 0;

        // ===== INPUT =====
        if (input.leftPressed && dashDuration == 0) {
            moveX = -MOVE_SPEED;
            direction = -1;
        }
        if (input.rightPressed && dashDuration == 0) {
            moveX = MOVE_SPEED;
            direction = 1;
        }

        boolean jumpPressed = input.consumeJumpJustPressed();
        boolean downPressed = input.downPressed;

        // ===== DROP DOWN =====
        if (downPressed && jumpPressed) {

            dropDownTimer = DROP_DOWN_DURATION;

            onGround = false;
            coyoteTime = 0;
            velocityY = 4;

            return;
        }

        // ===== JUMP BUFFER =====
        if (jumpPressed) {
            jumpBuffer = BUFFER_MAX;
        }

        if (jumpBuffer > 0)     jumpBuffer--;
        if (coyoteTime > 0)     coyoteTime--;
        if (dropDownTimer > 0)  dropDownTimer--;


        // ===== DASH =====
        if (input.dashPressed && !dashUsed && dashOnAirCount < MAX_DASH_ON_AIR) {   
            if(coyoteTime == 0)
            dashOnAirCount++;                        // Increment dash on air count if not on ground ( if you dash off from a high ground into air, you cannot dash again until you land, but if you dash on ground, you can dash again in air - Nhat)
            dashUsed = true;
            dashDuration = DASH_DURATION;                       // SET DASH DURATION (example value, adjust as needed)
            dashCooldown = DASH_COOLDOWN;     
            FALL_GRAVITY = 0;                    // SET DASH COOLDOWN (example value, adjust as needed)
        }

        if(dashDuration > 0 && dashUsed) {
            moveX = DASH_SPEED * direction;
            velocityY = 0;                                      // Cancel vertical velocity during dash but allow upward momentum to persist (you can still jump higher if you dash while moving upwards, but if you dash while falling, your fall will be stopped during the dash )
            FALL_GRAVITY = 0;                                        //Disable gravity during dash
        }
        
        // Update dash timers
        if (dashDuration > 0)  dashDuration--;
        if (dashCooldown > 0)  dashCooldown--;

        if (dashUsed && dashDuration == 0 )  {
                if(dashCooldown == 0) 
                    dashUsed = false;               // Allow dash again after cooldown
                FALL_GRAVITY = 0.7;                      // Reset gravity after dash
        }

        
        // ===== JUMP =====
        if (jumpBuffer > 0) {
            velocityY = JUMP_SPEED;
            onGround = false;
            dashDuration = 0;                       // Cancel dash if you jump
            GRAVITY = 1.2; 

            jumpBuffer = 0;
            coyoteTime = 0;
        }
         
        //      GOD MODE
        /*
        if (jumpBuffer > 0 ) {
            velocityY = JUMP_SPEED;

            onGround = false;
            jumpCount++;
            dashDuration = 0;                       // Cancel dash if you jump
            GRAVITY = 1.2; 

            jumpBuffer = 0;
            coyoteTime = 0;
        }
        if (input.dashPressed && !dashUsed && dashOnAirCount < MAX_DASH_ON_AIR) {                 
            dashUsed = true;
            dashDuration = DASH_DURATION;  
            dashCooldown = DASH_COOLDOWN;   
        }
        */
        //END GOD MODE
 
        // ===== PHYSICS =====
        if (velocityY < 0) {
            // đang nhảy lên
            velocityY += GRAVITY;
        } else {
            // đang rơi
            velocityY += FALL_GRAVITY;
        }

        if (velocityY > MAX_FALL) velocityY = MAX_FALL;

        // set velocityX
        velocityX = moveX;
        
        // ===== STATE =====
        updateState(moveX);

        // ===== ANIMATION =====
        updateAnimation();
    }

    public void setOnGround(boolean val) {
        if (val) {
            coyoteTime = COYOTE_MAX;
            jumpCount = 0;
            dashOnAirCount = 0;
        }
        this.onGround = val;
    }

    public boolean isDroppingDown() {
        return dropDownTimer > 0;
    }

    public boolean isIdle() {
        if (state == PlayerState.IDLE)
        return true;
        else return false; }

    public double getDirection() {
        return direction; 
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMp() {
        return mp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public int getDashCooldown() {
        return dashCooldown;
    }

    public int getDashCooldownMax() {
        return DASH_COOLDOWN;
    }

    public float getDashCooldownRatio() {
        if (DASH_COOLDOWN <= 0) {
            return 0f;
        }
        return (float) dashCooldown / DASH_COOLDOWN;
    }

    public void updateResources() {
        if (mp >= maxMp) {
            mpRegenAccumulator = 0;
            return;
        }

        mpRegenAccumulator += MP_REGEN_PER_FRAME;
        while (mpRegenAccumulator >= 1 && mp < maxMp) {
            mp++;
            mpRegenAccumulator -= 1;
        }
    }

    public boolean spendMp(int cost) {
        if (cost <= 0) {
            return true;
        }
        if (mp < cost) {
            return false;
        }
        mp -= cost;
        return true;
    }

    public void heal(int amount) {
        if (amount <= 0) {
            return;
        }
        hp = Math.min(maxHp, hp + amount);
    }

    public void restoreMp(int amount) {
        if (amount <= 0) {
            return;
        }
        mp = Math.min(maxMp, mp + amount);
    }

    public void takeDamage(int damage) {
        if (damage <= 0) {
            return;
        }
        hp = Math.max(0, hp - damage);
    }

    public Player(float x, float y) {
        super(x, y);
        loadSprite();
    }

    private void loadSprite() {
        try {
            var stream = getClass().getResourceAsStream("/assets/player/MIRAI_WITH_FRONT_ARM.png");

            if (stream == null) {
                System.out.println("KHoNG tim thay resource: /MIRAI_WITH_FRONT_ARM.png");
                return;
            }

            spriteSheet = ImageIO.read(stream);
            System.out.println("Load sprite thành công.");
        } catch (IOException e) {
            System.out.println("Lỗi khi load sprite.");
            e.printStackTrace();
        }
        
    }

    // ====== STATE ======
    private void updateState(double moveX) {
        switch (state) {
        case DASH:
        if (dashDuration <= 0) {
            // chỉ chuyển khi dash kết thúc
            state = (moveX != 0) ? PlayerState.RUN : PlayerState.IDLE;
        }
        break;

        default:
            if (dashDuration > 0) {
                state = PlayerState.DASH;
            } else if (!onGround) {
                if (velocityY < 0) state =  PlayerState.JUMP ;
                if (velocityY > 0 && coyoteTime < COYOTE_MAX/2) state =  PlayerState.FALL;
            } else if (moveX != 0) {
                state = PlayerState.RUN;
            } else {
            state = PlayerState.IDLE;
            }
        }
    }

    // ====== ANIMATION ======
    private void updateAnimation() {
        if (state != prevState) {
            currentFrame = 0;
            animationTick = 0;
            prevState = state;
        }

        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            currentFrame++;

            int max = getFrameCount();
            if (currentFrame >= max) currentFrame = 0;
        }
    }

    private int getFrameCount() {
        return switch (state) {
            case IDLE -> IDLE_FRAMES;
            case RUN -> RUN_FRAMES;
            case DASH -> DASH_FRAMES;
            case JUMP -> JUMP_FRAMES;
            case FALL -> FALL_FRAMES;
        };
    }

    private int getRow() {
        return switch (state) {
            case IDLE -> IDLE_ROW;
            case RUN -> RUN_ROW;
            case DASH -> DASH_ROW;
            case JUMP -> JUMP_ROW;
            case FALL -> FALL_ROW;
        };
    }

    

    private BufferedImage getFrame() {
        int x = currentFrame * FRAME_W;
        int y = getRow() * FRAME_H;
        return spriteSheet.getSubimage(x, y, FRAME_W, FRAME_H);
    }

    // ====== DRAW ======
    public void draw(Graphics2D g) {
        BufferedImage img = getFrame();

        /*  DEBUG YELLOW BOX OF DOOM AND DESPAIR 
        g.setColor(new Color(230, 190, 70)); // Placeholder color for player if sprite fails to load
        g.fillRect((int)x, (int)y, (int)(WIDTH) , (int)(HEIGHT));
        */
       
        if (direction == 1) {
            g.drawImage(img, (int)x + DRAW_OFFSET_X, (int)y + DRAW_OFFSET_Y, FRAME_W , FRAME_H , null); //
        } else {
            g.drawImage(img, (int)x + DRAW_OFFSET_X + FRAME_W , (int)y + DRAW_OFFSET_Y,
                    -FRAME_W ,FRAME_H, null);
        }

    }
}