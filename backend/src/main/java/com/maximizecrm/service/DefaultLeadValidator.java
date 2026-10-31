package com.maximizecrm.service;

import com.maximizecrm.repository.LeadRepository;
import org.springframework.stereotype.Component;

/**
 * Validates lead associations for contacts using LeadRepository.
 */
@Component
public class DefaultLeadValidator implements LeadValidator {

    private final LeadRepository leadRepository;

    public DefaultLeadValidator(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    public boolean hasAssociatedLeads(Long contactId) {
        if (contactId == null) {
            return false;
        }
        return leadRepository.existsByContactId(contactId);
    }
}
