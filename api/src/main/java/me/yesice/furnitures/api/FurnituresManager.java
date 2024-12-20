package me.yesice.furnitures.api;

import me.yesice.furnitures.api.objects.Furniture;

import java.util.List;
import java.util.Optional;

public interface FurnituresManager {

    Optional<Furniture> getFurniture(String id);

    List<Furniture> getFurnituresOfCategory(String category);
}
