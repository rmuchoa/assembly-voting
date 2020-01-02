package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VotingSessionRepository extends MongoRepository<VotingSession, String> {

    /**
     * Find optionally a voting session object by id property string value.
     *
     * @param id
     * @return
     */
    public Optional<VotingSession> findById(String id);

    /**
     * Find optionally a voting session object by agenda id property string value.
     *
     * @param agendaId
     * @return
     */
    public Optional<VotingSession> findByAgendaId(String agendaId);

    /**
     * Find all voting sessions that matches by status and closingTime is before another time value (like now).
     *
     * @param status
     * @param time
     * @return
     */
    public List<VotingSession> findByStatusAndClosingTimeBefore(String status, LocalDateTime time);

    /**
     * Find all voting sessions that matches by status and published property values.
     *
     * @param status
     * @param published
     * @return
     */
    public List<VotingSession> findByStatusAndPublished(String status, Boolean published);

}
