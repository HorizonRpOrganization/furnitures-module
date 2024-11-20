package me.yesice.furnitures.constants;

public enum Permissions {
    FURNITURES("furnitures.use"),
    BYPASS_FURNITURE_PLACE("furnitures.place.bypass"),
    BYPASS_FURNITURE_BREAK("furnitures.break.bypass");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return permission;
    }
}
