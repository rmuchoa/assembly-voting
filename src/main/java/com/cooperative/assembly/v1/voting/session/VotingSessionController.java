package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.response.ResponseJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("cooperative/assembly/v1/voting/session")
@Api(tags = "Voting Session")
public class VotingSessionController {

    private static final Long DEFAULT_DEADLINE_MINUTES = 1L;

    private VotingSessionService service;

    @Autowired
    public VotingSessionController(final VotingSessionService service) {
        this.service = service;
    }

    @ApiOperation(value = "Open Voting Session for Cooperative Assembly Agenda")
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseJson<VotingSessionResponse, Void>> open(
            @Valid @RequestBody VotingSessionRequest request) {

        if (request.getDeadlineMinutes() == null) {
            request.setDeadlineMinutes(DEFAULT_DEADLINE_MINUTES);
        }

        VotingSession votingSession = service.openFor(request.getAgendaId(), request.getDeadlineMinutes());
        return ResponseEntity.ok().body(VotingSessionResponse.buildResponse(votingSession));
    }

}
