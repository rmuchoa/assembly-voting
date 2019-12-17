package com.cooperative.assembly.voting.meeting.agenda;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MeetingAgendaRepository extends MongoRepository<MeetingAgenda, String> {

    /**
     * Find a meeting agenda obeject optionally by id property string value.
     *
     * @param id
     * @return
     */
    public Optional<MeetingAgenda> findById(String id);

    /**
     * Find a meeting agenda obeject optionally by title property string value.
     *
     * @param title
     * @return
     */
    public Optional<MeetingAgenda> findByTitle(String title);

}
