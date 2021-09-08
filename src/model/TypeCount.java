package model;

/**
 * This class is the model for a type count.
 */
public class TypeCount {

    /**
     * two private members make a type count object
     */
    private String type;
    private int count;

    /**
     * @param type name of a type
     * @param count count of a type
     */
    public TypeCount(String type, int count) {
        this.type = type;
        this.count = count;
    }

    /**
     * @return returns the count of a type
     */
    public int getCount() {
        return count;
    }

    /**
     * @return returns the name of a type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type sets the name of a type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Overrides the toString method
     * @return returns the name of a type along its count
     */
    @Override
    public String toString() {
        return type + ": " + count;
    }
}
