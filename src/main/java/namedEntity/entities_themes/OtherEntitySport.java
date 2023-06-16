package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.OtherEntity;
import namedEntity.themes.themes_classes.Sport;

public class OtherEntitySport extends OtherEntity implements Sport, Serializable {

    public OtherEntitySport(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public OtherEntitySport() {
        super();
        setCategory("OtherEntity");
        setTheme("Sport");
    }

}

