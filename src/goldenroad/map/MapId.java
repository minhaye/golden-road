package goldenroad.map;

public enum MapId {
    MAP_0,
    MAP_1,
    MAP_2,
    MAP_3;


    public MapId next() {
        return switch (this) {
            case MAP_0 -> MAP_1;
            case MAP_1 -> MAP_2;
            case MAP_2 -> MAP_3;
            case MAP_3 -> MAP_0;
        };
    }

    public String displayName() {
        return switch (this) {
            case MAP_0 -> "MAP 0";
            case MAP_1 -> "MAP 1";
            case MAP_2 -> "MAP 2";
            case MAP_3 -> "MAP 3";
        };
    }
}