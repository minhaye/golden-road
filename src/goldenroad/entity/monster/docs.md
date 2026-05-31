# Monster System Design for Java Game Project

## 1. Mục tiêu

Thiết kế hệ thống monster cho game Java theo hướng dễ mở rộng, dễ thêm monster mới và dễ load asset animation.

Monster trong game gồm 2 loại chính:

```txt
Monster
├── GroundMonster
└── AirborneMonster
```

Trong đó:

```txt
Monster: class cha, chứa logic chung
GroundMonster: quái đi dưới đất
AirborneMonster: quái bay trên không
```

Không cho `AirborneMonster` kế thừa từ `GroundMonster`.

---

## 2. Cấu trúc class

```txt
Monster
├── GroundMonster
└── AirborneMonster
```

### Monster

`Monster` là abstract class hoặc class cha.

Chứa các thuộc tính và hành vi chung của mọi monster.

### GroundMonster

`GroundMonster` kế thừa trực tiếp từ `Monster`.

Dùng cho monster di chuyển dưới mặt đất.

### AirborneMonster

`AirborneMonster` kế thừa trực tiếp từ `Monster`.

Dùng cho monster bay trên không.

---

## 3. Enum cần có

Nên dùng `enum` thay vì dùng string để tránh lỗi chính tả.

### MonsterState

```java
public enum MonsterState {
    IDLE,
    MOVE,
    ATTACK,
    HURT,
    DEATH
}
```

### Direction

```java
public enum Direction {
    LEFT,
    RIGHT
}
```

### MonsterType

```java
public enum MonsterType {
    GROUND,
    AIRBORNE
}
```

---

## 4. Class Monster

`Monster` chứa các thông tin chung:

```txt
hp
damage
attackSpeed
moveSpeed
width
height
spawnX
spawnY
x
y
moveRange
detectRange
attackRange
assets
currentState
direction
currentFrame
frameTimer
frameDuration
isDead
isAttacking
```

### Ý nghĩa thuộc tính

```txt
hp: máu của monster
damage: sát thương monster gây ra
attackSpeed: tốc độ tấn công
moveSpeed: tốc độ di chuyển
width: chiều rộng hitbox/sprite
height: chiều cao hitbox/sprite
spawnX, spawnY: vị trí spawn ban đầu
x, y: vị trí hiện tại
moveRange: phạm vi di chuyển quanh điểm spawn
detectRange: phạm vi phát hiện player
attackRange: khoảng cách đủ gần để tấn công
assets: danh sách frame animation theo từng state
currentState: trạng thái animation hiện tại
direction: hướng nhìn hiện tại
currentFrame: index frame hiện tại
frameTimer: bộ đếm thời gian đổi frame
frameDuration: thời gian hiển thị mỗi frame
isDead: monster đã chết hay chưa
isAttacking: monster đang tấn công hay không
```

### Kiểu dữ liệu gợi ý trong Java

```java
protected int hp;
protected int damage;

protected float attackSpeed;
protected float moveSpeed;

protected int width;
protected int height;

protected float x;
protected float y;
protected float spawnX;
protected float spawnY;

protected float moveRange;
protected float detectRange;
protected float attackRange;

protected Map<MonsterState, List<BufferedImage>> assets;

protected MonsterState currentState;
protected Direction direction;

protected int currentFrame;
protected float frameTimer;
protected float frameDuration;

protected boolean isDead;
protected boolean isAttacking;
```

### Hàm chung

```java
public void update(float deltaTime, Player player);
public void draw(Graphics2D g);
protected abstract void move(float deltaTime, Player player);
protected void attack(Player player);
public void takeDamage(int damage);
protected void die();
protected void updateAnimation(float deltaTime);
protected void setState(MonsterState newState);
```

---

## 5. Logic tấn công chung

Tất cả monster đều tấn công bằng cận chiến.

```txt
Nếu player ở ngoài detectRange:
- monster tuần tra quanh spawnPoint

Nếu player ở trong detectRange:
- monster di chuyển lại gần player

Nếu player ở trong attackRange:
- monster dừng di chuyển
- chuyển sang state ATTACK
- gây damage theo attackSpeed
```

Monster không bắn đạn, không tấn công tầm xa.

---

## 6. GroundMonster

`GroundMonster` kế thừa từ `Monster`.

### Đặc điểm

```txt
chỉ di chuyển trái/phải
chỉ thay đổi vị trí theo trục X
bị ràng buộc bởi mặt đất và địa hình
không bay lên/xuống
phạm vi di chuyển nhỏ quanh điểm spawn
```

### Logic di chuyển

```txt
Nếu không phát hiện player:
- đi tuần qua lại quanh spawnX
- không vượt quá moveRange

Nếu phát hiện player:
- nếu player ở bên trái thì đi sang trái
- nếu player ở bên phải thì đi sang phải
- nếu đủ gần thì tấn công

GroundMonster không tự thay đổi Y để bay.
Y phụ thuộc vào mặt đất, platform hoặc collision của map.
```

---

## 7. AirborneMonster

`AirborneMonster` kế thừa từ `Monster`.

### Đặc điểm

```txt
di chuyển được lên/xuống/trái/phải
có thể thay đổi cả X và Y
không bị ràng buộc bởi mặt đất
phạm vi di chuyển lớn hơn GroundMonster
có logic di chuyển riêng
```

### Logic di chuyển

```txt
Nếu không phát hiện player:
- bay tuần quanh spawnX, spawnY
- có thể di chuyển theo cả trục X và trục Y
- không vượt quá moveRange

Nếu phát hiện player:
- bay về phía player
- thay đổi cả X và Y
- nếu đủ gần thì tấn công
```

---

## 8. Cấu trúc thư mục asset

Vị trí: /src/assets/monster

Asset monster chia thành 2 nhóm:

```txt
airborne-monster
ground-monster
```

Mỗi nhóm có nhiều monster:

```txt
monster_1
monster_2
monster_3
monster_4
```

Mỗi monster có 5 animation state:

```txt
idle
move
attack
hurt
death
```

Cấu trúc chuẩn:

```txt
monster_1/
├── attack/
│   ├── attack_0.png
│   ├── attack_1.png
│   ├── attack_2.png
│   └── attack_3.png
├── death/
│   ├── death_0.png
│   ├── death_1.png
│   ├── death_2.png
│   └── death_3.png
├── hurt/
│   ├── hurt_0.png
│   └── hurt_1.png
├── idle/
│   ├── idle_0.png
│   └── idle_1.png
└── move/
    ├── move_0.png
    ├── move_1.png
    ├── move_2.png
    ├── move_3.png
    ├── move_4.png
    └── move_5.png
```

Số frame chuẩn:

```txt
idle: 2 frame
move: 6 frame
attack: 4 frame
hurt: 2 frame
death: 4 frame
```

---

## 9. Quy tắc load asset trong Java

Không hard-code số frame animation.

Code phải tự lấy số frame thật bằng:

```java
assets.get(currentState).size()
```

Ví dụ:

```java
List<BufferedImage> frames = assets.get(currentState);
int frameCount = frames.size();
```

Nếu sau này số frame bị lệch, animation vẫn chạy theo số frame thật trong folder.

---

## 10. Quy tắc animation

Các state chính:

```txt
IDLE
MOVE
ATTACK
HURT
DEATH
```

Khi đổi state, cần reset frame:

```java
protected void setState(MonsterState newState) {
    if (currentState != newState) {
        currentState = newState;
        currentFrame = 0;
        frameTimer = 0;
    }
}
```

Cập nhật animation:

```java
protected void updateAnimation(float deltaTime) {
    List<BufferedImage> frames = assets.get(currentState);

    if (frames == null || frames.isEmpty()) {
        return;
    }

    frameTimer += deltaTime;

    if (frameTimer >= frameDuration) {
        frameTimer -= frameDuration;
        currentFrame = (currentFrame + 1) % frames.size();
    }
}
```

Lấy ảnh hiện tại:

```java
BufferedImage currentImage = assets.get(currentState).get(currentFrame);
```

---

## 11. Tránh giật animation

Để animation không bị giật:

```txt
preload toàn bộ ảnh trước khi vào màn chơi
không load ảnh trong update()
dùng deltaTime
không hard-code số frame
các frame trong cùng animation nên cùng kích thước canvas
không đổi state liên tục nếu không cần thiết
attack, hurt, death nên có animation lock
```

---

## 12. Animation lock

Một số state không nên bị ngắt giữa chừng:

```txt
ATTACK
HURT
DEATH
```

Quy tắc:

```txt
Khi đang ATTACK:
- không chuyển ngay sang MOVE
- chờ animation attack chạy xong rồi mới đổi state

Khi đang HURT:
- có thể khóa ngắn
- sau khi animation hurt xong thì quay về IDLE hoặc MOVE

Khi đang DEATH:
- không được chuyển sang state khác
- chạy hết animation death rồi xóa monster khỏi game
```

---

## 13. Hướng asset và flip ảnh

Tất cả asset được thiết kế mặc định nhìn sang phải.

```txt
direction = RIGHT → vẽ ảnh gốc
direction = LEFT → flip ảnh theo trục X
```

Không cần tạo thêm asset riêng cho hướng trái.

Ví dụ vẽ flip trong Java:

```java
if (direction == Direction.LEFT) {
    g.drawImage(
        currentImage,
        (int) (x + width),
        (int) y,
        -width,
        height,
        null
    );
} else {
    g.drawImage(
        currentImage,
        (int) x,
        (int) y,
        width,
        height,
        null
    );
}
```

---

## 14. MonsterConfig

Mỗi monster cụ thể nên được tạo bằng config.

Không tạo class riêng cho từng monster nếu monster chỉ khác chỉ số và asset.

### Class MonsterConfig gợi ý

```java
public class MonsterConfig {
    public MonsterType type;
    public String name;

    public int hp;
    public int damage;

    public float attackSpeed;
    public float moveSpeed;

    public int width;
    public int height;

    public float moveRange;
    public float detectRange;
    public float attackRange;

    public float frameDuration;

    public String assetBasePath;

    public MonsterConfig(
            MonsterType type,
            String name,
            int hp,
            int damage,
            float attackSpeed,
            float moveSpeed,
            int width,
            int height,
            float moveRange,
            float detectRange,
            float attackRange,
            float frameDuration,
            String assetBasePath
    ) {
        this.type = type;
        this.name = name;
        this.hp = hp;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
        this.width = width;
        this.height = height;
        this.moveRange = moveRange;
        this.detectRange = detectRange;
        this.attackRange = attackRange;
        this.frameDuration = frameDuration;
        this.assetBasePath = assetBasePath;
    }
}
```

---

## 15. Ví dụ tạo GroundMonster

```java
MonsterConfig groundMonster1Config = new MonsterConfig(
    MonsterType.GROUND,
    "ground_monster_1",
    100,
    10,
    1.0f,
    2.0f,
    48,
    48,
    120f,
    200f,
    40f,
    120f,
    "assets/monster/ground-monster/monster_1"
);

GroundMonster monster = new GroundMonster(groundMonster1Config);
```

---

## 16. Ví dụ tạo AirborneMonster

```java
MonsterConfig airborneMonster1Config = new MonsterConfig(
    MonsterType.AIRBORNE,
    "airborne_monster_1",
    80,
    12,
    1.2f,
    2.5f,
    48,
    48,
    250f,
    250f,
    40f,
    120f,
    "assets/monster/airborne-monster/monster_1"
);

AirborneMonster monster = new AirborneMonster(airborneMonster1Config);
```

---

## 17. MonsterFactory

Nên có `MonsterFactory` để tạo monster dựa trên config.

```java
public class MonsterFactory {
    public static Monster createMonster(MonsterConfig config) {
        if (config.type == MonsterType.GROUND) {
            return new GroundMonster(config);
        }

        if (config.type == MonsterType.AIRBORNE) {
            return new AirborneMonster(config);
        }

        throw new IllegalArgumentException("Unknown monster type: " + config.type);
    }
}
```

Ví dụ dùng:

```java
Monster monster = MonsterFactory.createMonster(groundMonster1Config);
```

---

## 18. Yêu cầu khi AI generate code

Khi generate code Java, cần tuân thủ:

```txt
Dùng Java, không dùng JavaScript.
Không dùng const object kiểu JavaScript.
Dùng class MonsterConfig để lưu config.
Dùng enum MonsterState, Direction, MonsterType.

Không cho AirborneMonster kế thừa GroundMonster.
GroundMonster và AirborneMonster đều kế thừa trực tiếp từ Monster.

Không hard-code số frame animation.
Lấy số frame bằng assets.get(currentState).size().

Không load ảnh trong update().
Phải preload asset trước khi game chạy.

Dùng Map<MonsterState, List<BufferedImage>> để lưu animation frames.

GroundMonster chỉ thay đổi vị trí theo trục X.
AirborneMonster có thể thay đổi cả X và Y.

Tất cả monster dùng melee attack.
Monster di chuyển lại gần player nếu player ở trong detectRange.
Monster chỉ attack nếu player ở trong attackRange.

Asset mặc định nhìn sang phải.
Khi direction = LEFT thì flip ảnh theo trục X.

Các state ATTACK, HURT, DEATH nên có animation lock.
DEATH không được bị ngắt bởi state khác.

Mỗi monster cụ thể được tạo bằng config.
Không tạo class riêng cho từng monster nếu chỉ khác chỉ số và asset.
```

---

## 19. Kết luận

Thiết kế cuối cùng:

```txt
Monster là class cha
GroundMonster và AirborneMonster là 2 class con trực tiếp
mỗi monster cụ thể được tạo bằng MonsterConfig
asset được chia theo state animation
animation tự chạy theo số frame thật
hướng trái được xử lý bằng flip ảnh
```

Cách này phù hợp với project Java, dễ mở rộng và tránh nhầm logic giữa quái mặt đất và quái bay.
