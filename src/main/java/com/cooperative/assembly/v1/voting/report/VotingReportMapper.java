package com.cooperative.assembly.v1.voting.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Log4j2
@Component
public class VotingReportMapper {

    public Optional<String> toJson(final VotingReport report) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {

            return Optional.of(objectMapper.writeValueAsString(report));

        } catch (Exception ex) {
            log.error("Could not parse report to json: ", report, ex);
            return Optional.empty();
        }
    }

}
