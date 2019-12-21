package com.cooperative.assembly.vote;

import com.cooperative.assembly.response.ResponseJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Log4j2
@RestController
@RequestMapping("cooperative/assembly/vote")
@Api(tags = "Vote")
public class VoteController {

    private VoteService service;

    @Autowired
    public VoteController(final VoteService service) {
        this.service = service;
    }

    @ApiOperation(value = "Register Vote for Cooperative Assembly Agenda")
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseJson<VoteResponse, Void>> register(
            @Valid @RequestBody VoteRequest request) {

        Vote vote = service.chooseVote(request.getUserId(), request.getAgendaId(), request.getChoice());
        log.debug("Saved vote choice from user to voting agenda: ", vote);

        return ResponseEntity.ok().body(VoteResponse.buildResponse(vote));
    }
}
