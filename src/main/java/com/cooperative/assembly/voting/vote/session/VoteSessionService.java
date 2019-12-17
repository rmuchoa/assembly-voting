package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.voting.error.exception.ValidationException;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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
     * Open a vote session for a specific agenda created.
     * Only accept to open vote session for meeting agendas that have not been related to any vote session.
     * The related agenda is loaded by id and the vote period is defined between now and a deadline in minutes later.
     *
     * @param agendaId
     * @param deadlineMinutes
     * @return
     */
    public VoteSession openFor(final String agendaId, final Long deadlineMinutes) {
        if (!repository.findByAgendaId(agendaId).isEmpty()) {
            throw new ValidationException("vote.session.already.opened", "agendaId", agendaId);
        }

        MeetingAgenda agenda = meetingAgendaService.loadAgenda(agendaId);
        return openFor(agenda, deadlineMinutes);
    }

    private VoteSession openFor(final MeetingAgenda agenda, Long deadlineMinutes) {
        String id = UUID.randomUUID().toString();
        LocalDateTime openingTime = LocalDateTime.now();
        LocalDateTime closingTime = LocalDateTime.now().plusMinutes(deadlineMinutes);

        VoteSession voteSession = new VoteSession(id, agenda, openingTime, closingTime);
        return repository.save(voteSession);
    }

}
