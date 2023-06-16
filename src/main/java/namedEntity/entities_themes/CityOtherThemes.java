package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.placeSubclass.City;
import namedEntity.themes.themes_classes.OtherThemes;

public class CityOtherThemes extends City implements OtherThemes, Serializable {

    public CityOtherThemes(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public CityOtherThemes() {
        super();
        setCategory("City");
        setTheme("OtherThemes");
    }

}

