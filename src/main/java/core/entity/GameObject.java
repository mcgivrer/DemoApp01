package core.entity;

import java.awt.image.BufferedImage;

public class GameObject extends Entity<GameObject> {

    private BufferedImage image;

    public GameObject(String name) {
        super(name);
    }

    public GameObject setSprite(BufferedImage image) {
        this.image = image;
        return this;
    }

    public BufferedImage getSprite() {
        return image;
    }

}
