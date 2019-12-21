package com.cooperative.assembly.vote.counting;

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
@RequestMapping("cooperative/assembly/vote/counting")
@Api(tags = "Vote Counting")
public class VoteCountingController {

    private VoteCountingService service;

    @Autowired
    public VoteCountingController(final VoteCountingService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get Vote Counting for Cooperative Assembly Agenda")
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseJson<VoteCountingResponse, Void>> getVoteCounting(
            @Valid VoteCountingRequest request) {

        VoteCounting counting = service.getVoteCounting(request.getAgendaId());

        return ResponseEntity.ok().body(VoteCountingResponse.buildResponse(counting));
    }

}
