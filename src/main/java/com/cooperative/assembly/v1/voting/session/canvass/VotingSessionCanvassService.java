package com.cooperative.assembly.v1.voting.session.canvass;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;

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

    /**
     * Load all opened voting session canvasses.
     *
     * @return
     */
    public List<VotingSessionCanvass> loadOpenedSessionCanvass() {
        return repository.findByStatus(OPENED.toString());
    }

    /**
     * Load all closed voting session canvasses that was not published yet.
     *
     * @return
     */
    public List<VotingSessionCanvass> loadClosedSessionCanvassToPublish() {
        return repository.findByStatusAndPublished(CLOSED.toString(), FALSE);
    }

}
