package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.Product;
import namedEntity.themes.themes_classes.politicsSubclass.National;

public class ProductNational extends Product implements National, Serializable {

    public ProductNational(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public ProductNational() {
        super();
        setCategory("Product");
        setTheme("National");
    }

}

