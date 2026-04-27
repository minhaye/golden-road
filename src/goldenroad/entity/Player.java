package goldenroad.entity;
    
import goldenroad.input.KeyHandler;

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
    private static final int DRAW_OFFSET_X = -85; 
    private static final int DRAW_OFFSET_Y = -95;
    private static final double SCREEN_SCALE = 3;
    public double SCALE = 3; 
    // Default to 720p, can be changed to 1080p or 1440p by adjusting the denominator 
    // (example: for 1080p, use 1.25, for 1440p, use 1.25/1.5)
    // x2 = 1280x720 = 720p
    // x3 = 1920x1080 = 1080p
    // x4 = 2560x1440 = 1440p
    // x6 = 3840x2160 = 4K

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
    private boolean onGround = true;

    private double direction = 1;

    private double MOVE_SPEED = 4 * SCALE;
    private double GRAVITY = 1.2;
    private double JUMP_SPEED = -8.5 * SCALE;
    private double MAX_FALL = 10.0 * SCALE;

    private int jumpCount = 0;
    private static final int MAX_JUMPS = 2;

    // ====== DASH ======
    private double DASH_SPEED = 10 * 3;
    private int dashDuration = 0;
    private int dashCooldown = 0;
    private boolean dashUsed = false;
    private int dashOnAirCount = 0;
    private static final int MAX_DASH_ON_AIR = 1;


    private static final int DASH_DURATION = 15;
    private static final int DASH_COOLDOWN = 35;

    // ====== COYOTE + BUFFER ======
    private int coyoteTime = 0;
    private int jumpBuffer = 0;

    private static final int COYOTE_MAX = 12;
    private static final int BUFFER_MAX = 12;

    // ====== SIZE ======
    private double WIDTH = 20 * SCALE;
    private double HEIGHT = 45 * SCALE;

    public float getX() { return x; }
    public float getY() { return y; }

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

    // ====== UPDATE ======
    public void update(KeyHandler input, List<Rectangle> blocks) {
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

        // ===== JUMP BUFFER =====
        if (input.consumeJumpJustPressed()) {
            jumpBuffer = BUFFER_MAX;
        }

        if (jumpBuffer > 0) jumpBuffer--;
        if (coyoteTime > 0) coyoteTime--;

        // ===== DASH =====
       /*
        if (input.dashPressed && !dashUsed && dashOnAirCount < MAX_DASH_ON_AIR) {
            if(coyoteTime == 0)
            dashOnAirCount++;                                   // Increment dash on air count if not on ground ( if you dash off from a high ground into air, you cannot dash again until you land, but if you dash on ground, you can dash again in air - Nhat)
            dashUsed = true;
            dashDuration = DASH_DURATION;                       // SET DASH DURATION (example value, adjust as needed)
            dashCooldown = DASH_COOLDOWN;                       // SET DASH COOLDOWN (example value, adjust as needed)
        }
          */
        if(dashDuration > 0 && dashUsed) {
            moveX = DASH_SPEED * direction;
            velocityY = 0;                                      // Cancel vertical velocity during dash but allow upward momentum to persist (you can still jump higher if you dash while moving upwards, but if you dash while falling, your fall will be stopped during the dash )
            GRAVITY = 0;                                        //Disable gravity during dash
        }
        
        // Update dash timers
        if (dashDuration > 0)  dashDuration--;
        if (dashCooldown > 0)  dashCooldown--;

        if (dashUsed && dashDuration == 0 )  {
                if(dashCooldown == 0) 
                    dashUsed = false;               // Allow dash again after cooldown
                GRAVITY = 1.2;                      // Reset gravity after dash
        }

        // ===== JUMP =====
        /*
        if (jumpBuffer > 0 && (coyoteTime > 0 || jumpCount < MAX_JUMPS)) {
            velocityY = JUMP_SPEED;
            if (onGround == false && coyoteTime == 0) {
            jumpCount++;
            }
            onGround = false;
            jumpCount++;
            dashDuration = 0;                       // Cancel dash if you jump
            GRAVITY = 1.2; 

            jumpBuffer = 0;
            coyoteTime = 0;
        }
         */

        
        //      GOD MODE
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
                                 // Increment dash on air count if not on ground ( if you dash off from a high ground into air, you cannot dash again until you land, but if you dash on ground, you can dash again in air - Nhat)
            dashUsed = true;
            dashDuration = DASH_DURATION;                       // SET DASH DURATION (example value, adjust as needed)
            dashCooldown = DASH_COOLDOWN;     
            GRAVITY = 0;                    // SET DASH COOLDOWN (example value, adjust as needed)
        }

        //END GOD MODE
        /**/

        // ===== PHYSICS =====
        velocityY += GRAVITY;
        if (velocityY > MAX_FALL) velocityY = MAX_FALL;

        applyHorizontal(moveX, blocks);
        applyVertical(velocityY, blocks);

        // ===== STATE =====
        updateState(moveX);

        // ===== ANIMATION =====
        updateAnimation();
    }

    // ====== MOVEMENT ======
    private void applyHorizontal(double dx, List<Rectangle> blocks) {
        double nextX = x + dx;
        Rectangle next = getBounds(nextX, y);

        for (Rectangle r : blocks) {
            if (next.intersects(r)) {
                if (dx > 0) nextX = r.x - WIDTH;
                else nextX = r.x + r.width;
            }
        }
        x = (float) nextX;
    }

    private void applyVertical(double dy, List<Rectangle> blocks) {
        double nextY = y + dy;
        Rectangle next = getBounds(x, nextY);

        onGround = false;

        for (Rectangle r : blocks) {
            if (next.intersects(r)) {
                if (dy > 0) {
                    nextY = r.y - HEIGHT;
                    onGround = true;
                    coyoteTime = COYOTE_MAX;
                    jumpCount = 0;
                } else {
                    nextY = r.y + r.height;
                }
                velocityY = 0;
            }
        }
        y = (float) nextY;                                      // Update playerY after handling vertical collisions
        if (onGround) {
            jumpCount = 0;
            dashOnAirCount = 0;                                 // Reset dash on air count when landing
        }
    }

    // Hit box
    private Rectangle getBounds(double x, double y) {
        return new Rectangle((int)x, (int)y, (int)WIDTH, (int)HEIGHT);
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

    public boolean isIdle() {
        if (state == PlayerState.IDLE)
        return true;
        else return false;
    }

    public double getDirection() {
    return direction;
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

        g.setColor(new Color(230, 190, 70)); // Placeholder color for player if sprite fails to load
        g.fillRect((int)x, (int)y, (int)(WIDTH) , (int)(HEIGHT));

        if (direction == 1) {
            g.drawImage(img, (int)x + DRAW_OFFSET_X, (int)y + DRAW_OFFSET_Y, (int) (FRAME_W * 2.4), (int) (FRAME_H * 2.4), null); //
        } else {
            g.drawImage(img, (int)x + (int) (DRAW_OFFSET_X + FRAME_W * 2.4), (int)y + DRAW_OFFSET_Y,
                    (int) (-FRAME_W * 2.4),  (int) (FRAME_H * 2.4), null);
        }
    }
}