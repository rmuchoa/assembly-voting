package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.agenda.VotingAgendaService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;
import static java.util.UUID.randomUUID;
import static java.time.LocalDateTime.now;

@Log4j2
@Service
public class VotingSessionService {

    private static final Boolean NOT_PUBLISHED = FALSE;

    private VotingSessionRepository repository;
    private VotingAgendaService votingAgendaService;

    @Autowired
    public VotingSessionService(final VotingSessionRepository repository, final VotingAgendaService votingAgendaService) {
        this.repository = repository;
        this.votingAgendaService = votingAgendaService;
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

        VotingSession votingSession = new VotingSession(id, agenda, openingTime, closingTime, OPENED, FALSE);
        log.debug("Save voting session to start voting");
        return repository.save(votingSession);
    }

    /**
     * Load voting session by id.
     * Throw NotFoundReferenceException.class when voting session can not be found.
     *
     * @param id
     * @return
     */
    public VotingSession loadVoteSession(final String id) {
        Optional<VotingSession> session = repository.findById(id);
        if (!session.isPresent()) {
            log.error("Voting session was not found", id);
            throw new NotFoundReferenceException("VotingSession", "voting.session.not.found");
        }

        return session.get();
    }

    /**
     * Load voting session by agenda id related
     * Throw NotFoundReferenceException.class when voting session can not be found.
     *
     * @param agendaId
     * @return
     */
    public VotingSession loadVoteSessionByAgenda(final String agendaId) {
        Optional<VotingSession> session = repository.findByAgendaId(agendaId);
        if (!session.isPresent()) {
            log.error("Voting session was not found for agenda", agendaId);
            throw new NotFoundReferenceException("VotingSession", "voting.session.not.found");
        }

        return session.get();
    }

    /**
     * Load all opened voting sessions canvass that should not be opened yet.
     *
     * @return
     */
    public List<VotingSession> loadMissClosedSessions() {
        return repository.findByStatusAndClosingTimeBefore(OPENED.toString(), now());
    }

    /**
     * Load all current closed voting sessions that was not published yet.
     *
     * @return
     */
    public List<VotingSession> loadClosedSessionsToPublish() {
        return repository.findByStatusAndPublished(CLOSED.toString(), NOT_PUBLISHED);
    }

    /**
     * Save voting session with new status
     *
     * @param session
     * @return
     */
    public VotingSession saveSession(final VotingSession session) {
        log.debug("Save session status to allow publish voting counting results");
        return repository.save(session);
    }

}
