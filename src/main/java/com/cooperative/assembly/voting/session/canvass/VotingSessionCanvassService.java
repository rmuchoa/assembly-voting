package com.cooperative.assembly.voting.session.canvass;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class VotingSessionCanvassService {

    private VotingSessionCanvassRepository repository;

    @Autowired
    public VotingSessionCanvassService(final VotingSessionCanvassRepository repository) {
        this.repository = repository;
    }

    public VotingSessionCanvass saveCanvass(final VotingSessionCanvass canvass) {
        log.debug("Save session canvass to allow publish voting counting results");
        return repository.save(canvass);
    }

}
