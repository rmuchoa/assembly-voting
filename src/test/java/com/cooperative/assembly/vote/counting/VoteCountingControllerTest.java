package com.cooperative.assembly.vote.counting;

import com.cooperative.assembly.error.ResponseErrorHandler;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@EnableSpringDataWebSupport
@ContextConfiguration(classes = { VoteCountingController.class, ResponseErrorHandler.class })
public class VoteCountingControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private VoteCountingService service;

    @Autowired
    private WebApplicationContext context;

    private String sessionUUID;
    private String agendaUUID;
    private String canvassId;
    private String agendaTitle;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;
    private VotingAgenda agenda;
    private VotingSession session;
    private VotingSessionCanvass canvass;
    private String requestFormatErrorCode;
    private String incorrectRequestFormat;
    private String agendaIdField;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.sessionUUID = randomUUID().toString();
        this.agendaUUID = "2b6f8057-cd5e-4a20-afa0-c04419a8983b";
        this.canvassId = randomUUID().toString();
        this.agendaTitle = "Eleição de Diretoria";
        this.openingTime = now().withNano(0);
        this.closingTime = now().withNano(0).plusMinutes(2);
        this.totalVotes = 35;
        this.affirmativeVotes = 22;
        this.negativeVotes = 13;
        this.agenda = new VotingAgenda(agendaUUID, agendaTitle);
        this.canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        this.session = new VotingSession(sessionUUID, agenda, canvass, openingTime, closingTime);

        this.requestFormatErrorCode = "ERR0100";
        this.incorrectRequestFormat = "Incorrect request format";
        this.agendaIdField = "agendaId";
    }

    @Test
    public void shouldReturnResponseDataAndNotReturnErrorsWhenPerformSuccessVotingSessionOpening() throws Exception {
        final ResultActions result = performSuccessProcessing();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformProcessCountingWithEmptyAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithEmptyAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformOpeningSessionWithNullAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithNullAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithExtraSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithMinorSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithMinorSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithWronglyFormattedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithWronglyFormattedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnOpenedVotingSessionWithGeneratedUUIDAndPeriodTimes() throws Exception {
        final ResultActions result = performSuccessProcessing();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agenda").value(agendaTitle))
                .andExpect(jsonPath("$.data.totalVotes").value(totalVotes))
                .andExpect(jsonPath("$.data.affirmativeVotes").value(affirmativeVotes))
                .andExpect(jsonPath("$.data.negativeVotes").value(negativeVotes))
                .andExpect(jsonPath("$.data.openingTime").value(openingTime.toString()))
                .andExpect(jsonPath("$.data.closingTime").value(closingTime.toString()));
    }

    @Test
    public void shouldReturnNotEmptyAndInvalidSizedResponseErrorWhenTryingToPerformVoteCountingProcessWithEmptyAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithEmptyAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.counting.agenda.id.not.empty", "vote.counting.agenda.id.invalid.size", "vote.counting.agenda.id.invalid.uuid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("", "", "")));
    }

    @Test
    public void shouldReturnInvalidAndNotEmptyResponseErrorWhenTryingToPerformVoteCountingProcessWithNullAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithNullAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.counting.agenda.id.not.empty"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(agendaIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").doesNotExist());
    }

    @Test
    public void shouldReturnInvalidSizeResponseErrorWhenTryingToPerformVoteCountingProcessWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithExtraSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.counting.agenda.id.invalid.size", "vote.counting.agenda.id.invalid.uuid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("2b6f8057-cd5e-4a20-afa0-c04419a8983b-2b6f8057", "2b6f8057-cd5e-4a20-afa0-c04419a8983b-2b6f8057")));
    }

    @Test
    public void shouldReturnInvalidSizeResponseErrorWhenTryingToPerformVoteCountingProcessWithMinorSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithMinorSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.counting.agenda.id.invalid.size", "vote.counting.agenda.id.invalid.uuid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("2b6f8057-cd5e-4a20-afa0", "2b6f8057-cd5e-4a20-afa0")));
    }

    @Test
    public void shouldReturnInvalidFormatResponseErrorWhenTryingToPerformVoteCountingProcessWithWronglyFormattedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryProcessCountingWithWronglyFormattedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.counting.agenda.id.invalid.uuid.format"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(agendaIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value("2b6f8057-cd5e-4a20-afa0c04419a898-3b"));
    }

    private ResultActions performSuccessProcessing() throws Exception {
        VoteCounting counting = new VoteCounting(agendaTitle, openingTime, closingTime, totalVotes, affirmativeVotes, negativeVotes);
        when(service.getVoteCounting(agendaUUID)).thenReturn(counting);

        return mockMvc.perform(get("/cooperative/assembly/vote/counting")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("agendaId", "2b6f8057-cd5e-4a20-afa0-c04419a8983b"));
    }

    private ResultActions tryProcessCountingWithEmptyAgendaId() throws Exception {
        return mockMvc.perform(get("/cooperative/assembly/vote/counting")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("agendaId", ""));
    }

    private ResultActions tryProcessCountingWithNullAgendaId() throws Exception {
        return mockMvc.perform(get("/cooperative/assembly/vote/counting")
                .contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    private ResultActions tryProcessCountingWithExtraSizedAgendaId() throws Exception {
        return mockMvc.perform(get("/cooperative/assembly/vote/counting")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("agendaId", "2b6f8057-cd5e-4a20-afa0-c04419a8983b-2b6f8057"));
    }

    private ResultActions tryProcessCountingWithMinorSizedAgendaId() throws Exception {
        return mockMvc.perform(get("/cooperative/assembly/vote/counting")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("agendaId", "2b6f8057-cd5e-4a20-afa0"));
    }

    private ResultActions tryProcessCountingWithWronglyFormattedAgendaId() throws Exception {
        return mockMvc.perform(get("/cooperative/assembly/vote/counting")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("agendaId", "2b6f8057-cd5e-4a20-afa0c04419a898-3b"));
    }

}
