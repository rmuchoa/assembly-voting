package com.cooperative.assembly.voting.session;

import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaService;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;
import static java.util.UUID.randomUUID;

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
