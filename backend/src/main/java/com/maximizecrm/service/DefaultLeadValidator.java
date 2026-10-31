package com.maximizecrm.service;

import org.springframework.stereotype.Component;

/**
 * Validates lead associations for contacts.
 * Leads module is not yet implemented. When the Leads module is added,
 * this component can be updated to query the lead repository.
 */
@Component
public class DefaultLeadValidator implements LeadValidator {

    @Override
    public boolean hasAssociatedLeads(Long contactId) {
        // No leads exist yet in the system.
        return false;
    }
}

