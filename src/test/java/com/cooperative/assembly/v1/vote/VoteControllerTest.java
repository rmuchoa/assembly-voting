package com.cooperative.assembly.v1.vote;

import com.cooperative.assembly.error.ResponseErrorHandler;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
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

import static com.cooperative.assembly.v1.vote.VoteChoice.YES;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.lang.Boolean.FALSE;
import static java.util.UUID.randomUUID;
import static java.time.LocalDateTime.now;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@EnableSpringDataWebSupport
@ContextConfiguration(classes = { VoteController.class, ResponseErrorHandler.class })
public class VoteControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private VoteService service;

    @Autowired
    private WebApplicationContext context;

    @Value("classpath:/requestChooseVote.json")
    private Resource requestChooseVote;

    @Value("classpath:/requestChooseVoteWithFormattedUserId.json")
    private Resource requestChooseVoteWithFormattedUserId;

    @Value("classpath:/requestEmptyUserIdVoteRegister.json")
    private Resource requestEmptyUserIdVoteRegister;

    @Value("classpath:/requestNullUserIdVoteRegister.json")
    private Resource requestNullUserIdVoteRegister;

    @Value("classpath:/requestExtraSizedUserIdVoteRegister.json")
    private Resource requestExtraSizedUserIdVoteRegister;

    @Value("classpath:/requestMalformedCPFUserIdVoteRegister.json")
    private Resource requestMalformedCPFUserIdVoteRegister;

    @Value("classpath:/requestEmptyAgendaIdVoteRegister.json")
    private Resource requestEmptyAgendaIdVoteRegister;

    @Value("classpath:/requestNullAgendaIdVoteRegister.json")
    private Resource requestNullAgendaIdVoteRegister;

    @Value("classpath:/requestExtraSizedAgendaIdVoteRegister.json")
    private Resource requestExtraSizedAgendaIdVoteRegister;

    @Value("classpath:/requestMalformedUUIDAgendaIdVoteRegister.json")
    private Resource requestMalformedUUIDAgendaIdVoteRegister;

    @Value("classpath:/requestNullChoiceVoteRegister.json")
    private Resource requestNullChoiceVoteRegister;

    @Value("classpath:/requestMalformedEnumChoiceVoteRegister.json")
    private Resource requestMalformedEnumChoiceVoteRegister;

    private String voteUUID;
    private String agendaUUID;
    private String sessionUUID;
    private String canvassUUID;
    private String userId;
    private String agendaTitle;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;
    private String requestFormatErrorCode;
    private String incorrectRequestFormat;
    private String userIdField;
    private String sessionIdField;
    private String choiceField;
    private VotingAgenda agenda;
    private VotingSessionCanvass canvass;
    private VotingSession session;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.voteUUID = randomUUID().toString();
        this.agendaUUID = randomUUID().toString();
        this.canvassUUID = randomUUID().toString();
        this.sessionUUID = "2b6f8057-cd5e-4a20-afa0-c04419a8983b";
        this.userId = "12429593009";
        this.agendaTitle = "Eleição de Diretoria";
        this.openingTime = now().withNano(0);
        this.closingTime = openingTime.minusMinutes(5);
        this.totalVotes = 10;
        this.affirmativeVotes = 6;
        this.negativeVotes = 4;
        this.agenda = new VotingAgenda(agendaUUID, agendaTitle);
        this.session = new VotingSession(sessionUUID, agenda, openingTime, closingTime, OPENED, FALSE);
        this.canvass = new VotingSessionCanvass(canvassUUID, agendaTitle, totalVotes, affirmativeVotes, negativeVotes, session);
        this.requestFormatErrorCode = "ERR0100";
        this.incorrectRequestFormat = "Incorrect request format";
        this.userIdField = "userId";
        this.sessionIdField = "sessionId";
        this.choiceField = "choice";
    }

    @Test
    public void shouldReturnResponseDataAndNotReturnResponseErrorsWhenPerformSuccessVoteChoice() throws Exception {
        final ResultActions result = performSuccessRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnRegisteredVoteWithGeneratedUUIDAndSessionReference() throws Exception {
        final ResultActions result = performSuccessRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(voteUUID))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.session").exists())
                .andExpect(jsonPath("$.data.session.id").value(sessionUUID))
                .andExpect(jsonPath("$.data.session.agenda").exists())
                .andExpect(jsonPath("$.data.session.agenda.id").value(agendaUUID))
                .andExpect(jsonPath("$.data.session.agenda.title").value(agendaTitle))
                .andExpect(jsonPath("$.data.session.status").value(OPENED.toString()))
                .andExpect(jsonPath("$.data.choice").value(YES.toString()));
    }

    @Test
    public void shouldReturnResponseDataAndNotReturnResponseErrorsWhenPerformSuccessVoteChoiceWithFormattedUserId() throws Exception {
        final ResultActions result = performSuccessRegisterWithFormattedUserId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnRegisteredVoteWithGeneratedUUIDAndSessionReferenceWithFormattedUserId() throws Exception {
        final ResultActions result = performSuccessRegisterWithFormattedUserId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(voteUUID))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.session").exists())
                .andExpect(jsonPath("$.data.session.id").value(sessionUUID))
                .andExpect(jsonPath("$.data.session.agenda").exists())
                .andExpect(jsonPath("$.data.session.agenda.id").value(agendaUUID))
                .andExpect(jsonPath("$.data.session.agenda.title").value(agendaTitle))
                .andExpect(jsonPath("$.data.session.status").value(OPENED.toString()))
                .andExpect(jsonPath("$.data.choice").value(YES.toString()));
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithEmptyUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformEmptyUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithNullUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithExtraSizedUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformExtraSizedUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithMalformedCPFUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformMalformedCPFUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithEmptyAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformEmptyAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithNullAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformExtraSizedAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithMalformedUUIDAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformMalformedUUIDAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformRegisterWithNullChoiceRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullChoicePropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnNotEmptyResponseErrorWhenTryingToPerformRegisterWithEmptyUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformEmptyUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.user.id.not.empty", "vote.user.id.invalid.cpf.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(userIdField, userIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("", "")));
    }

    @Test
    public void shouldReturnNotNullResponseErrorWhenTryingToPerformRegisterWithNullUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.user.id.not.empty"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(userIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").doesNotExist());
    }

    @Test
    public void shouldReturnExtraSizedResponseErrorWhenTryingToPerformRegisterWithExtraSizedUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformExtraSizedUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[*].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[*].detail").value("vote.user.id.invalid.cpf.format"))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer").value(userIdField))
                .andExpect(jsonPath("$.errors[*].source.parameter").value("8635833104386358331043"));
    }

    @Test
    public void shouldReturnMalformedCPFResponseErrorWhenTryingToPerformRegisterWithMalformedCPFUserIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformMalformedCPFUserIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.user.id.invalid.cpf.format"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(userIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value("86E58EE1O4E"));
    }

    @Test
    public void shouldReturnNotEmptyResponseErrorWhenTryingToPerformRegisterWithEmptyAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformEmptyAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.session.id.not.empty", "vote.session.id.invalid.uuid.format", "vote.session.id.invalid.size")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(sessionIdField, sessionIdField, sessionIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("", "", "")));
    }

    @Test
    public void shouldReturnNotNullResponseErrorWhenTryingToPerformRegisterWithNullAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.session.id.not.empty"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(sessionIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").doesNotExist());
    }

    @Test
    public void shouldReturnExtraSizedResponseErrorWhenTryingToPerformRegisterWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformExtraSizedAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.session.id.invalid.uuid.format", "vote.session.id.invalid.size")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(sessionIdField, sessionIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("2b6f8057-cd5e-4a20-afa0-c04419a8983b-2b6f8057", "2b6f8057-cd5e-4a20-afa0-c04419a8983b-2b6f8057")));
    }

    @Test
    public void shouldReturnMalformedUUIDResponseErrorWhenTryingToPerformRegisterWithMalformedUUIDAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformMalformedUUIDAgendaIdPropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.session.id.invalid.uuid.format"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(sessionIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value("2b6f8057-cd5e-4a20-afa0c04419a8-983b"));
    }

    @Test
    public void shouldReturnNotNullResponseErrorWhenTryingToPerformRegisterWithNullChoiceRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullChoicePropertyRegister();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.choice.not.null"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(choiceField))
                .andExpect(jsonPath("$.errors[0].source.parameter").doesNotExist());
    }

    private ResultActions performSuccessRegister() throws Exception {
        Vote vote = new Vote(voteUUID, userId, session, YES);
        when(service.chooseVote(userId, sessionUUID, YES)).thenReturn(vote);

        final String bodyContent = Resources.toString(requestChooseVote.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions performSuccessRegisterWithFormattedUserId() throws Exception {
        Vote vote = new Vote(voteUUID, userId, session, YES);
        when(service.chooseVote(userId, sessionUUID, YES)).thenReturn(vote);

        final String bodyContent = Resources.toString(requestChooseVoteWithFormattedUserId.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformEmptyUserIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestEmptyUserIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformNullUserIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestNullUserIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformExtraSizedUserIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestExtraSizedUserIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformMalformedCPFUserIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestMalformedCPFUserIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformEmptyAgendaIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestEmptyAgendaIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformNullAgendaIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestNullAgendaIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformExtraSizedAgendaIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestExtraSizedAgendaIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformMalformedUUIDAgendaIdPropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestMalformedUUIDAgendaIdVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformNullChoicePropertyRegister() throws Exception {
        final String bodyContent = Resources.toString(requestNullChoiceVoteRegister.getURL(), UTF_8);
        return mockMvc.perform(post("/cooperative/assembly/v1/vote")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

}
