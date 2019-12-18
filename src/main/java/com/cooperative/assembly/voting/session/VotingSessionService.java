package com.cooperative.assembly.voting.session;

import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class VotingSessionService {

    @Autowired
    private VotingSessionRepository repository;

    @Autowired
    private VotingAgendaService votingAgendaService;

    @Autowired
    public VotingSessionService(final VotingSessionRepository repository) {
        this.repository = repository;
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
        if (hasVoteSessionAlredyOpenedFor(agendaId)) {
            throw new ValidationException("vote.session.already.opened", "agendaId", agendaId);
        }

        VotingAgenda agenda = loadSessionAgenda(agendaId);
        return openSessionFor(agenda, deadlineMinutes);
    }

    /**
     * Load session a specific agenda that was previously created by id.
     *
     * @param agendaId
     * @return
     */
    protected VotingAgenda loadSessionAgenda(final String agendaId) {
        Optional<VotingAgenda> agenda = votingAgendaService.loadAgenda(agendaId);
        if (!agenda.isPresent()) {
            throw new NotFoundReferenceException("VotingAgenda", "meeting.agenda.not.found");
        }

        return agenda.get();
    }

    /**
     * Check if given agenda already is related to some vote session.
     *
     * @param agendaId
     * @return
     */
    private Boolean hasVoteSessionAlredyOpenedFor(final String agendaId) {
        List<VotingSession> foundSessions = repository.findByAgendaId(agendaId);
        return !isEmpty(foundSessions);
    }

    /**
     * Open session for related agenda defining time period for voting by deadline in minutes.
     *
     * @param agenda
     * @param deadlineMinutes
     * @return
     */
    protected VotingSession openSessionFor(final VotingAgenda agenda, Long deadlineMinutes) {
        String id = UUID.randomUUID().toString();
        LocalDateTime openingTime = LocalDateTime.now();
        LocalDateTime closingTime = LocalDateTime.now().plusMinutes(deadlineMinutes);

        VotingSession votingSession = new VotingSession(id, agenda, openingTime, closingTime);
        return repository.save(votingSession);
    }

}
