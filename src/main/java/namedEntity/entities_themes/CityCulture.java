package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.placeSubclass.City;
import namedEntity.themes.themes_classes.Culture;

public class CityCulture extends City implements Culture, Serializable {

    public CityCulture(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public CityCulture() {
        super();
        setCategory("City");
        setTheme("Culture");
    }

}

