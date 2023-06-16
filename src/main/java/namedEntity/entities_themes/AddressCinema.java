package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.placeSubclass.Address;
import namedEntity.themes.themes_classes.cultureSubclass.Cinema;

public class AddressCinema extends Address implements Cinema, Serializable {

    public AddressCinema(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public AddressCinema() {
        super();
        setCategory("Address");
        setTheme("Cinema");
    }

}

