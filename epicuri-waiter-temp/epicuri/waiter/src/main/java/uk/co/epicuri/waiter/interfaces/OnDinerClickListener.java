package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

/**
 * Interface definition for a callback to be invoked when this a diner is selected for this session
 *
 * @author Pete Harris <peteh@thedistance.co.uk>
 */
public interface OnDinerClickListener {
    /**
     * fired when a diner is selected, or deselected
     *
     * @param diner the selected diner, or null if nothing selected
     */
    void onDinerClick(EpicuriSessionDetail.Diner diner);
}

