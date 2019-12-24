package com.cooperative.assembly.v1.voting.session.canvass;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VotingSessionCanvassRepository extends MongoRepository<VotingSessionCanvass, String> {

    /**
     * List all voting session canvasses by filtered status.
     *
     * @param status
     * @return
     */
    List<VotingSessionCanvass> findByStatus(String status);

    /**
     * List all voting session canvasses by filtered status and boolean published.
     *
     * @param status
     * @return
     */
    List<VotingSessionCanvass> findByStatusAndPublished(String status, Boolean published);

}
