package com.cooperative.assembly.voting.meeting.agenda;

import com.cooperative.assembly.voting.error.exception.NotFoundReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class MeetingAgendaService {

    private MeetingAgendaRepository repository;

    @Autowired
    public MeetingAgendaService(final MeetingAgendaRepository repository) {
        this.repository = repository;
    }

    /**
     * Create new agenda for Cooperative assembly meeting.
     * This agenda will be related to open a voting session later.
     *
     * @param title
     * @return
     */
    public MeetingAgenda create(final String title) {
        String id = UUID.randomUUID().toString();
        MeetingAgenda meetingAgenda = new MeetingAgenda(id, title);

        return repository.save(meetingAgenda);
    }

    /**
     * Load some agenda based on a specific agenda id.
     * This agenda can be related on a voting session.
     * Throws a NotFoundReferenceException.class when agenda can not be found.
     *
     * @param id
     * @return
     */
    public MeetingAgenda loadAgenda(final String id) {
        Optional<MeetingAgenda> agenda = repository.findById(id);
        if (agenda.isPresent()) {
            return agenda.get();
        }

        throw new NotFoundReferenceException("MeetingAgenda", "meeting.agenda.not.found");
    }

}
