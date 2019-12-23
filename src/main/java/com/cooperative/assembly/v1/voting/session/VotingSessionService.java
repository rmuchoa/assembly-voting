package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.agenda.VotingAgendaService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Log4j2
@Service
public class VotingSessionService {

    private VotingSessionRepository repository;
    private VotingAgendaService votingAgendaService;
    private VotingSessionCanvassService votingSessionCanvassService;

    @Autowired
    public VotingSessionService(final VotingSessionRepository repository, final VotingAgendaService votingAgendaService, final VotingSessionCanvassService votingSessionCanvassService) {
        this.repository = repository;
        this.votingAgendaService = votingAgendaService;
        this.votingSessionCanvassService = votingSessionCanvassService;
    }

    /**
     * Open a vote session for related agenda by id defining a deadline time ranged in minutes.
     * Only accept to open vote session for meeting agendas that have not been related to any vote session.
     *
     * @param agendaId
     * @param deadlineMinutes
     * @return
     */
    public VotingSession openFor(final String agendaId, final Long deadlineMinutes) {
        Optional<VotingSession> session = repository.findByAgendaId(agendaId);
        if (session.isPresent()) {
            log.error("There is already an opened voting session for this agenda", agendaId);
            throw new ValidationException("voting.session.already.opened", "agendaId", agendaId);
        }

        VotingAgenda agenda = loadSessionAgenda(agendaId);
        return openSessionFor(agenda, deadlineMinutes);
    }

    /**
     * Load specific agenda by id that was previously created.
     * Throw NotFoundReferenceException.class when voting agenda can not be found.
     *
     * @param agendaId
     * @return
     */
    protected VotingAgenda loadSessionAgenda(final String agendaId) {
        Optional<VotingAgenda> agenda = votingAgendaService.loadAgenda(agendaId);
        if (!agenda.isPresent()) {
            log.error("Voting agenda was not found to open session", agendaId);
            throw new NotFoundReferenceException("VotingAgenda", "voting.agenda.not.found");
        }

        return agenda.get();
    }

    /**
     * Load voting session by agenda that was previously related with.
     * Throw NotFoundReferenceException.class when voting session can not be found.
     *
     * @param agendaId
     * @return
     */
    public VotingSession loadVoteSession(final String agendaId) {
        Optional<VotingSession> session = repository.findByAgendaId(agendaId);
        if (!session.isPresent()) {
            log.error("Voting session was not found to vote on it's agenda", agendaId);
            throw new NotFoundReferenceException("VotingSession", "voting.session.not.found");
        }

        return session.get();
    }

    /**
     * Open session for related agenda defining time period for voting by deadline in minutes.
     *
     * @param agenda
     * @param deadlineMinutes
     * @return
     */
    protected VotingSession openSessionFor(final VotingAgenda agenda, Long deadlineMinutes) {
        String id = randomUUID().toString();
        LocalDateTime openingTime = LocalDateTime.now();
        LocalDateTime closingTime = LocalDateTime.now().plusMinutes(deadlineMinutes);

        VotingSessionCanvass canvass = createSessionCanvass(agenda);

        VotingSession votingSession = new VotingSession(id, agenda, canvass, openingTime, closingTime);
        log.debug("Save voting session to start voting");
        return repository.save(votingSession);
    }

    /**
     * Build and save new session canvass for voting session.
     *
     * @param agenda
     * @return
     */
    protected VotingSessionCanvass createSessionCanvass(final VotingAgenda agenda) {
        VotingSessionCanvass emptyCanvass = buildNewSessionCanvas(agenda);
        log.error("Building new voting session canvas", emptyCanvass);

        return votingSessionCanvassService.saveCanvass(emptyCanvass);
    }

    /**
     * Build empty session canvass for init voting session
     *
     * @param agenda
     * @return
     */
    protected VotingSessionCanvass buildNewSessionCanvas(final VotingAgenda agenda) {
        String id = randomUUID().toString();
        return new VotingSessionCanvass(id, agenda.getTitle(), 0, 0, 0);
    }

}
