package goldenroad.entity.player;

import goldenroad.input.KeyHandler;

public class PlayerMovement {
    // movement state
    private double velocityY = 0;
    private double velocityX = 0;
    private boolean onGround = true;
    private double direction = 1;

    // constants
    private double MOVE_SPEED = 6;
    private double GRAVITY = 1.2;
    private double FALL_GRAVITY = 0.7;
    private double JUMP_SPEED = -17;
    private double MAX_FALL = 12.0;

    private int jumpCount = 0;
    private static final int MAX_JUMPS = 2;
    private int dropDownTimer = 0;
    private static final int DROP_DOWN_DURATION = 10;

    // dash
    private double DASH_SPEED = 10;
    private int dashDuration = 0;
    private int dashCooldown = 0;
    private boolean dashUsed = false;
    private int dashOnAirCount = 0;
    private static final int MAX_DASH_ON_AIR = 1;

    private static final int DASH_DURATION = 20;
    private static final int DASH_COOLDOWN = 40;

    // coyote + buffer
    private int coyoteTime = 0;
    private int jumpBuffer = 0;

    private static final int COYOTE_MAX = 12;
    private static final int BUFFER_MAX = 12;

    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public void setVelocityY(double v) { this.velocityY = v; }

    public int getDashDuration() { return dashDuration; }
    public boolean isOnGround() { return onGround; }
    public int getCoyoteTime() { return coyoteTime; }

    public void setOnGround(boolean val) {
        if (val) {
            coyoteTime = COYOTE_MAX;
            jumpCount = 0;
            dashOnAirCount = 0;
        }
        this.onGround = val;
    }

    public boolean isDroppingDown() { return dropDownTimer > 0; }

    public double getDirection() { return direction; }

    public int getDashCooldown() { return dashCooldown; }
    public int getDashCooldownMax() { return DASH_COOLDOWN; }
    public float getDashCooldownRatio() {
        if (DASH_COOLDOWN <= 0) return 0f;
        return (float) dashCooldown / DASH_COOLDOWN;
    }

    // Performs movement update and returns the final horizontal moveX used
    public double update(KeyHandler input) {
        double moveX = 0;

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

        // drop down
        if (downPressed && jumpPressed) {
            dropDownTimer = DROP_DOWN_DURATION;
            onGround = false;
            coyoteTime = 0;
            velocityY = 4;
            return 0;
        }

        if (jumpPressed) jumpBuffer = BUFFER_MAX;
        if (jumpBuffer > 0) jumpBuffer--;
        if (coyoteTime > 0) coyoteTime--;
        if (dropDownTimer > 0) dropDownTimer--;

        // dash — chi kich hoat khi nhan Shift (edge), khong dash khi giu phim
        if (input.consumeDashJustPressed() && !dashUsed && dashOnAirCount < MAX_DASH_ON_AIR) {
            if (coyoteTime == 0) dashOnAirCount++;
            dashUsed = true;
            dashDuration = DASH_DURATION;
            dashCooldown = DASH_COOLDOWN;
            FALL_GRAVITY = 0;
        }

        if (dashDuration > 0 && dashUsed) {
            moveX = DASH_SPEED * direction;
            velocityY = 0;
            FALL_GRAVITY = 0;
        }

        if (dashDuration > 0) dashDuration--;
        if (dashCooldown > 0) dashCooldown--;

        if (dashUsed && dashDuration == 0) {
            if (dashCooldown == 0) dashUsed = false;
            FALL_GRAVITY = 0.7;
        }

        // jump
        if (jumpBuffer > 0) {
            velocityY = JUMP_SPEED;
            onGround = false;
            dashDuration = 0;
            GRAVITY = 1.2;
            jumpBuffer = 0;
            coyoteTime = 0;
        }

        // physics
        if (velocityY < 0) velocityY += GRAVITY;
        else velocityY += FALL_GRAVITY;

        if (velocityY > MAX_FALL) velocityY = MAX_FALL;

        velocityX = moveX;

        return moveX;
    }
}
