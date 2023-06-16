package namedEntity.entities_themes;

import java.io.Serializable;

import namedEntity.entities.entity_classes.Company;
import namedEntity.themes.themes_classes.politicsSubclass.International;

public class CompanyInternational extends Company implements International, Serializable {

    public CompanyInternational(String name, String category, int frequency) {
        super(name, category, frequency);
    }

    public CompanyInternational() {
        super();
        setCategory("Company");
        setTheme("International");
    }

}

