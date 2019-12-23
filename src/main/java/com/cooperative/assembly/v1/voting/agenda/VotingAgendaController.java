package com.cooperative.assembly.v1.voting.agenda;

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
@RequestMapping("cooperative/assembly/v1/voting/agenda")
@Api(tags = "Voting Agenda")
public class VotingAgendaController {

    private VotingAgendaService service;

    @Autowired
    public VotingAgendaController(VotingAgendaService service) {
        this.service = service;
    }

    @ApiOperation(value = "Create Voting Agenda for Cooperative Assembly")
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseJson<VotingAgendaResponse, Void>> create(
            @Valid @RequestBody VotingAgendaRequest request) {

        VotingAgenda votingAgenda = service.create(request.getTitle());
        log.debug("Created voting agenda to allow start voting session", votingAgenda);

        return ResponseEntity.ok().body(VotingAgendaResponse.buildResponse(votingAgenda));
    }

}
