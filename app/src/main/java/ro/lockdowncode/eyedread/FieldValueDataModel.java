package ro.lockdowncode.eyedread;

/**
 * Created by Adi Neag on 23.07.2018.
 */

public class FieldValueDataModel {

    String name;
    String value;

    public FieldValueDataModel(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
