package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class is the model for a country.
 */
public class Country {

    /**
     * two private members make a country object
     * an observable list holds all usable countries
     */
    private int countryId;
    private String country;
    public static ObservableList<Country> allCountries = FXCollections.observableArrayList();


    /**
     * @param countryId unique ID of a country
     * @param country name of a country
     */
    public Country(int countryId, String country) {
        this.countryId = countryId;
        this.country = country;
    }

    /**
     * @return returns ID of a country
     */
    public int getCountryId() {
        return countryId;
    }

    /**
     * @return returns name of a country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Overrides the toString method
     * @return returns the country name
     */
    @Override
    public String toString() {
        return country;
    }
}
