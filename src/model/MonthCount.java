package model;

/**
 * This class is the model for a month count.
 */
public class MonthCount {

    /**
     * two private members make a month count object
     */
    private String month;
    private int count;

    /**
     * @param month name of the month
     * @param count count of the month
     */
    public MonthCount(String month, int count) {
        this.month = month;
        this.count = count;
    }

    /**
     * @return returns the count of the month
     */
    public int getCount() {
        return count;
    }

    /**
     * @return returns the name of the month
     */
    public String getMonth() {
        return month;
    }

    /**
     * Overrides the toString method
     * @return returns the name of the month along with its count
     */
    @Override
    public String toString() {
        return month + ": " + count;
    }
}