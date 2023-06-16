package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.Person;
import namedEntity.themes.themes_classes.politicsSubclass.National;

public class PersonNational extends Person implements National, Serializable {

    public PersonNational(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public PersonNational() {
        super();
        setCategory("Person");
        setTheme("National");
    }

}

