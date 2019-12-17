package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.response.ResponseJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("vote/session")
@Api(tags = "Vote Session")
public class VoteSessionController {

    private static final Long DEFAULT_DEADLINE_MINUTES = 1L;

    private VoteSessionService service;

    @Autowired
    public VoteSessionController(final VoteSessionService service) {
        this.service = service;
    }

    @ApiOperation(value = "Create Meeting Agenda for Cooperative Assembly")
    @PostMapping(value = "/open",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseJson<VoteSessionResponse, Void>> open(@Valid @RequestBody VoteSessionRequest request) {
        if (request.getDeadlineMinutes() == null) {
            request.setDeadlineMinutes(DEFAULT_DEADLINE_MINUTES);
        }

        VoteSession voteSession = service.openFor(request.getAgendaId(), request.getDeadlineMinutes());
        return ResponseEntity.ok().body(VoteSessionResponse.buildResponse(voteSession));
    }

}
