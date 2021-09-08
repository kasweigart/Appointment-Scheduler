package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

/**
 * This class is the model for a first level division.
 */


public class FirstLevelDivision {

    /**
     * three private members create a first level division object
     */
    private int divisionId;
    private String division;
    private int countryId;

    /**
     * four observable lists holds specific sets of first level divisions
     */
    public static ObservableList<FirstLevelDivision> allFirstLevelDivisions = FXCollections.observableArrayList();
    public static ObservableList<FirstLevelDivision> unitedStatesFirstLevelDivisions =
            FXCollections.observableArrayList();
    public static ObservableList<FirstLevelDivision> unitedKingdomFirstLevelDivisions =
            FXCollections.observableArrayList();
    public static ObservableList<FirstLevelDivision> canadaFirstLevelDivisions = FXCollections.observableArrayList();

    /**
     * @param divisionId unique ID of a division
     * @param division name of a division
     * @param countryId unique ID of a country associated to a division
     */
    public FirstLevelDivision(int divisionId, String division, int countryId) {
        this.divisionId = divisionId;
        this.division = division;
        this.countryId = countryId;
    }

    /**
     * @return returns all first level divisions associated with the United States
     */
    public static ObservableList<FirstLevelDivision> populateUSFirstLevelDivisions() {
        for (FirstLevelDivision firstLevelDivision : allFirstLevelDivisions) {
            if (firstLevelDivision.getCountryId() == 1) {
                unitedStatesFirstLevelDivisions.add(firstLevelDivision);
            }
        }

        return unitedStatesFirstLevelDivisions;
    }

    /**
     * @return returns all first level division associated with the United Kingdom
     */
    public static ObservableList<FirstLevelDivision> populateUKFirstLevelDivisions() {
        for (FirstLevelDivision firstLevelDivision : allFirstLevelDivisions) {
            if (firstLevelDivision.getCountryId() == 2) {
                unitedKingdomFirstLevelDivisions.add(firstLevelDivision);
            }
        }
        return unitedKingdomFirstLevelDivisions;
    }

    /**
     * @return returns all first level divisions associated with Canada
     */
    public static ObservableList<FirstLevelDivision> populateCanadaFirstLevelDivisions() {
        for (FirstLevelDivision firstLevelDivision : allFirstLevelDivisions) {
            if (firstLevelDivision.getCountryId() == 3) {
                canadaFirstLevelDivisions.add(firstLevelDivision);
            }
        }
        return canadaFirstLevelDivisions;
    }

    /**
     * @return returns the ID of a division
     */
    public int getDivisionId() {
        return divisionId;
    }

    /**
     * @return returns the name of a division
     */
    public String getDivision() {
        return division;
    }

    /**
     * @return returns the ID of a country
     */
    public int getCountryId() {
        return countryId;
    }

    /**
     * Overrides the toString method
     * @return returns the name of a division
     */
    @Override
    public String toString() {
        return division;
    }
}
