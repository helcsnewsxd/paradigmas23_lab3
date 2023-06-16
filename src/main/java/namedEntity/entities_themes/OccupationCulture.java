package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.personSubclass.Occupation;
import namedEntity.themes.themes_classes.Culture;

public class OccupationCulture extends Occupation implements Culture, Serializable {

    public OccupationCulture(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public OccupationCulture() {
        super();
        setCategory("Occupation");
        setTheme("Culture");
    }

}

