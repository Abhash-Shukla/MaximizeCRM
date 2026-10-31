package com.maximizecrm;

import com.maximizecrm.entity.Lead;
import com.maximizecrm.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MaximizeCrmApplicationTests {

    @Autowired
    private LeadRepository leadRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void searchLeadsQueryTest() {
        List<Lead> leads = leadRepository.searchLeads(null, null, null);
        assertNotNull(leads);
    }
}
