package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.voting.error.exception.ValidationException;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class VoteSessionService {

    @Autowired
    private VoteSessionRepository repository;

    @Autowired
    private MeetingAgendaService meetingAgendaService;

    @Autowired
    public VoteSessionService(final VoteSessionRepository repository) {
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
    public VoteSession openFor(final String agendaId, final Long deadlineMinutes) {
        if (hasVoteSessionAlredyOpenedFor(agendaId)) {
            throw new ValidationException("vote.session.already.opened", "agendaId", agendaId);
        }

        MeetingAgenda agenda = loadSessionAgenda(agendaId);
        return openSessionFor(agenda, deadlineMinutes);
    }

    /**
     * Load session a specific agenda that was previously created by id.
     *
     * @param agendaId
     * @return
     */
    protected MeetingAgenda loadSessionAgenda(final String agendaId) {
        Optional<MeetingAgenda> agenda = meetingAgendaService.loadAgenda(agendaId);
        if (!agenda.isPresent()) {
            throw new NotFoundReferenceException("MeetingAgenda", "meeting.agenda.not.found");
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
        List<VoteSession> foundSessions = repository.findByAgendaId(agendaId);
        return !isEmpty(foundSessions);
    }

    /**
     * Open session for related agenda defining time period for voting by deadline in minutes.
     *
     * @param agenda
     * @param deadlineMinutes
     * @return
     */
    protected VoteSession openSessionFor(final MeetingAgenda agenda, Long deadlineMinutes) {
        String id = UUID.randomUUID().toString();
        LocalDateTime openingTime = LocalDateTime.now();
        LocalDateTime closingTime = LocalDateTime.now().plusMinutes(deadlineMinutes);

        VoteSession voteSession = new VoteSession(id, agenda, openingTime, closingTime);
        return repository.save(voteSession);
    }

}
