package model;

/**
 * This class is the model for a contact.
 */
public class Contact {

    /**
     * three private members create a contact object
     */
    private int contactId;
    private String contactName;
    private String email;

    /**
     * @param contactId unique ID of a contact
     * @param contactName name of a contact
     * @param email email of a contact
     */
    public Contact(int contactId, String contactName, String email) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.email = email;
    }

    /**
     * @return returns ID of a contact
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * @return returns name of a contact
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * @return returns email of a contact
     */
    public String getEmail() {
        return email;
    }

    /**
     * Overrides the toString method
     * @return returns the name of the contact
     */
    @Override
    public String toString() {
        return contactName;
    }
}
