package model;
//enum is a special type used when we have a fixed,limited set of option
public enum PlaceType {
    RELIEF_HUB("Relief Hub"),
    AFFECTED_AREA("Affected Area");

    private final String label;
    //This is the constructor for the enum.
    PlaceType(String label) {
        this.label = label; //When RELIEF_HUB or AFFECTED_AREA are created by Java, this code takes the string in the parentheses and saves it into the label variable.
    }

    //Name shown in dropdown menus on the Map tab.
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
//Every object in Java has a default toString() method, which dictates how the object is represented when converted to text (like when you print it to the console or put it in a dropdown menu).

//By default, printing PlaceType.RELIEF_HUB would output the exact constant name: "RELIEF_HUB".

//By overriding this method to return label, printing it will instead output the nice, readable "Relief Hub".
