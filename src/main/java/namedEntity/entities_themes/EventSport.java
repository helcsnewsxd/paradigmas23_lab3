package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.Event;
import namedEntity.themes.themes_classes.Sport;

public class EventSport extends Event implements Sport, Serializable {

    public EventSport(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public EventSport() {
        super();
        setCategory("Event");
        setTheme("Sport");
    }

}

