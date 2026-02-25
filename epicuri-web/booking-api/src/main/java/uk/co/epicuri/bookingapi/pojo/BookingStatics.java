package uk.co.epicuri.bookingapi.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 01/05/2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingStatics {
    private String entityName = "";
    private String entityNumber = "";
    private String entityEmail = "";
    private String backLabel = "";
    private String dateStepMessage = "";
    private String dateStepButtonLabel = "";
    private String timeStepMessage = "";
    private String timeStepButtonLabel = "";
    private String detailsStepMessage = "";
    private String detailsStepButtonLabel = "";
    private String nameLabel = "";
    private String numberLabel = "";
    private String emailLabel = "";
    private String notesLabel = "";
    private String confirmationMessage = "";
    private String telephoneValidationError = "";
    private String nameValidationError = "";
    private String title = "";
    private List<String> times = new ArrayList<>();
    private String token;
    private String marketingMessage = "";
    /*
    ISO 639-1 language code
     */
    private String language = "en";
    private String confirmationStepMessage = "";
    private String biggerPartiesLabel = "";
    private String toleranceBreachMessage = "";
    private String timeInPastMessage = "";
    private String noTimeSlotsMessage = "";
    private String numberGuestsLabel = "";

    public String getEntityEmail() {
        return entityEmail;
    }

    public void setEntityEmail(String entityEmail) {
        this.entityEmail = entityEmail;
    }

    public String getNumberGuestsLabel() {
        return numberGuestsLabel;
    }

    public void setNumberGuestsLabel(String numberGuestsLabel) {
        this.numberGuestsLabel = numberGuestsLabel;
    }

    public String getNoTimeSlotsMessage() {
        return noTimeSlotsMessage;
    }

    public void setNoTimeSlotsMessage(String noTimeSlotsMessage) {
        this.noTimeSlotsMessage = noTimeSlotsMessage;
    }

    public String getTimeInPastMessage() {
        return timeInPastMessage;
    }

    public void setTimeInPastMessage(String timeInPastMessage) {
        this.timeInPastMessage = timeInPastMessage;
    }

    public String getConfirmationStepMessage() {
        return confirmationStepMessage;
    }

    public void setConfirmationStepMessage(String confirmationStepMessage) {
        this.confirmationStepMessage = confirmationStepMessage;
    }

    public String getBiggerPartiesLabel() {
        return biggerPartiesLabel;
    }

    public void setBiggerPartiesLabel(String biggerPartiesLabel) {
        this.biggerPartiesLabel = biggerPartiesLabel;
    }

    public String getToleranceBreachMessage() {
        return toleranceBreachMessage;
    }

    public void setToleranceBreachMessage(String toleranceBreachMessage) {
        this.toleranceBreachMessage = toleranceBreachMessage;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMarketingMessage() {
        return marketingMessage;
    }

    public void setMarketingMessage(String marketingMessage) {
        this.marketingMessage = marketingMessage;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityNumber() {
        return entityNumber;
    }

    public void setEntityNumber(String entityNumber) {
        this.entityNumber = entityNumber;
    }

    public String getBackLabel() {
        return backLabel;
    }

    public void setBackLabel(String backLabel) {
        this.backLabel = backLabel;
    }

    public String getDateStepMessage() {
        return dateStepMessage;
    }

    public void setDateStepMessage(String dateStepMessage) {
        this.dateStepMessage = dateStepMessage;
    }

    public String getDateStepButtonLabel() {
        return dateStepButtonLabel;
    }

    public void setDateStepButtonLabel(String dateStepButtonLabel) {
        this.dateStepButtonLabel = dateStepButtonLabel;
    }

    public String getTimeStepMessage() {
        return timeStepMessage;
    }

    public void setTimeStepMessage(String timeStepMessage) {
        this.timeStepMessage = timeStepMessage;
    }

    public String getTimeStepButtonLabel() {
        return timeStepButtonLabel;
    }

    public void setTimeStepButtonLabel(String timeStepButtonLabel) {
        this.timeStepButtonLabel = timeStepButtonLabel;
    }

    public String getDetailsStepMessage() {
        return detailsStepMessage;
    }

    public void setDetailsStepMessage(String detailsStepMessage) {
        this.detailsStepMessage = detailsStepMessage;
    }

    public String getDetailsStepButtonLabel() {
        return detailsStepButtonLabel;
    }

    public void setDetailsStepButtonLabel(String detailsStepButtonLabel) {
        this.detailsStepButtonLabel = detailsStepButtonLabel;
    }

    public String getNameLabel() {
        return nameLabel;
    }

    public void setNameLabel(String nameLabel) {
        this.nameLabel = nameLabel;
    }

    public String getNumberLabel() {
        return numberLabel;
    }

    public void setNumberLabel(String numberLabel) {
        this.numberLabel = numberLabel;
    }

    public String getEmailLabel() {
        return emailLabel;
    }

    public void setEmailLabel(String emailLabel) {
        this.emailLabel = emailLabel;
    }

    public String getNotesLabel() {
        return notesLabel;
    }

    public void setNotesLabel(String notesLabel) {
        this.notesLabel = notesLabel;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public String getTelephoneValidationError() {
        return telephoneValidationError;
    }

    public void setTelephoneValidationError(String telephoneValidationError) {
        this.telephoneValidationError = telephoneValidationError;
    }

    public String getNameValidationError() {
        return nameValidationError;
    }

    public void setNameValidationError(String nameValidationError) {
        this.nameValidationError = nameValidationError;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    @Override
    public String toString() {
        return "BookingStatics{" +
                "entityName='" + entityName + '\'' +
                ", entityNumber='" + entityNumber + '\'' +
                ", entityEmail='" + entityEmail + '\'' +
                ", backLabel='" + backLabel + '\'' +
                ", dateStepMessage='" + dateStepMessage + '\'' +
                ", dateStepButtonLabel='" + dateStepButtonLabel + '\'' +
                ", timeStepMessage='" + timeStepMessage + '\'' +
                ", timeStepButtonLabel='" + timeStepButtonLabel + '\'' +
                ", detailsStepMessage='" + detailsStepMessage + '\'' +
                ", detailsStepButtonLabel='" + detailsStepButtonLabel + '\'' +
                ", nameLabel='" + nameLabel + '\'' +
                ", numberLabel='" + numberLabel + '\'' +
                ", emailLabel='" + emailLabel + '\'' +
                ", notesLabel='" + notesLabel + '\'' +
                ", confirmationMessage='" + confirmationMessage + '\'' +
                ", telephoneValidationError='" + telephoneValidationError + '\'' +
                ", nameValidationError='" + nameValidationError + '\'' +
                ", title='" + title + '\'' +
                ", times=" + times +
                ", token='" + token + '\'' +
                ", marketingMessage='" + marketingMessage + '\'' +
                ", language='" + language + '\'' +
                ", confirmationStepMessage='" + confirmationStepMessage + '\'' +
                ", biggerPartiesLabel='" + biggerPartiesLabel + '\'' +
                ", toleranceBreachMessage='" + toleranceBreachMessage + '\'' +
                ", timeInPastMessage='" + timeInPastMessage + '\'' +
                ", noTimeSlotsMessage='" + noTimeSlotsMessage + '\'' +
                ", numberGuestsLabel='" + numberGuestsLabel + '\'' +
                '}';
    }
}
