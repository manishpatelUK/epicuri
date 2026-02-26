package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriCustomer;

public interface NewPartyDialogListener {
    void onCreateNewParty(CharSequence partyName, int numberInParty, EpicuriCustomer customer);
    void onCreateNewSession(CharSequence partyName, int numberInParty, EpicuriCustomer customer, String[] tables, String serviceId);
}

