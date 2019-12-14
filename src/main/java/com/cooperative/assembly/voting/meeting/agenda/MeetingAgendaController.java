package com.cooperative.assembly.voting.meeting.agenda;

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
@RequestMapping("meeting/agenda")
@Api(tags = "Meeting Agenda")
public class MeetingAgendaController {

    private MeetingAgendaService service;

    @Autowired
    public MeetingAgendaController(MeetingAgendaService service) {
        this.service = service;
    }

    @ApiOperation(value = "Create Meeting Agenda for Cooperative Assembly")
    @PostMapping(value = "/create",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseJson<MeetingAgendaResponse, Void>> create(@Valid @RequestBody MeetingAgendaRequest request) {
        MeetingAgenda meetingAgenda = service.create(request.getTitle());
        return ResponseEntity.ok().body(MeetingAgendaResponse.buildResponse(meetingAgenda));
    }

}
