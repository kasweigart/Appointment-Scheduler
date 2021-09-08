package model;

/**
 * This class is the model for a customer.
 */
public class Customer {

    /**
     * six private members create a customer object
     */
    private int customerId;
    private String customerName;
    private String customerAddress;
    private String firstLevelDivision;
    private String postalCode;
    private String phoneNumber;

    /**
     * @param customerId unique ID of a customer
     * @param customerName name of a customer
     * @param customerAddress address of a customer
     * @param firstLevelDivision first level division associated with a customer
     * @param postalCode postal code of a customer
     * @param phoneNumber phone number of a customer
     */
    public Customer(int customerId, String customerName, String customerAddress, String firstLevelDivision,
                    String postalCode,
                    String phoneNumber) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.firstLevelDivision = firstLevelDivision;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return returns first level division associated with a customer
     */
    public String getFirstLevelDivision() {

        return firstLevelDivision; }

    /**
     * @return returns ID of a customer
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * @return returns name of a customer
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @return returns address of a customer
     */
    public String getCustomerAddress() {
        return customerAddress;
    }

    /**
     * @return returns postal code of a customer
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @return returns phone number of a customer
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
