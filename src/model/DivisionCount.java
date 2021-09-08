package model;

/**
 * This class is the model of a division count.
 */
public class DivisionCount {

    /**
     * two private members create a division count object
     */
    private String division;
    private int count;

    /**
     * @param division name of a divison
     * @param count count of a division
     */
    public DivisionCount(String division, int count) {
        this.division = division;
        this.count = count;
    }

    /**
     * @return returns count of a division
     */
    public int getCount() {
        return count;
    }

    /**
     * @return returns name of a divison
     */
    public String getDivision() {
        return division;
    }

    /**
     * Overrides toString method
     * @return returns the name of the division along with its count
     */
    @Override
    public String toString() {
        return division + ": " + count;
    }
}