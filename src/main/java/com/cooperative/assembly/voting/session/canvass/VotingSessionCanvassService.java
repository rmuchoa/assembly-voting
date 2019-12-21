package com.cooperative.assembly.voting.session.canvass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VotingSessionCanvassService {

    private VotingSessionCanvassRepository repository;

    @Autowired
    public VotingSessionCanvassService(final VotingSessionCanvassRepository repository) {
        this.repository = repository;
    }

    public VotingSessionCanvass saveCanvass(final VotingSessionCanvass canvass) {
        return repository.save(canvass);
    }

}
