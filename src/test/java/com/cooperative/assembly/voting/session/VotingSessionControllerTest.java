package com.cooperative.assembly.voting.session;

import com.cooperative.assembly.error.ResponseErrorHandler;
import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
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
import static java.util.UUID.randomUUID;
import static java.time.LocalDateTime.now;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@EnableSpringDataWebSupport
@ContextConfiguration(classes = { VotingSessionController.class, ResponseErrorHandler.class })
public class VotingSessionControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private VotingSessionService votingSessionService;

    @Autowired
    private WebApplicationContext context;

    @Value("classpath:/requestOpenVotingSession.json")
    private Resource requestOpenVotingSession;

    @Value("classpath:/requestEmptyAgendaIdVotingSessionOpening.json")
    private Resource requestEmptyAgendaIdVotingSessionOpening;

    @Value("classpath:/requestNullAgendaIdVotingSessionOpening.json")
    private Resource requestNullAgendaIdVotingSessionOpening;

    @Value("classpath:/requestExtraSizedAgendaIdVotingSessionOpening.json")
    private Resource requestExtraSizedAgendaIdVotingSessionOpening;

    @Value("classpath:/requestMinorSizedAgendaIdVotingSessionOpening.json")
    private Resource requestMinorSizedAgendaIdVotingSessionOpening;

    @Value("classpath:/requestWronglyFormattedAgendaIdVotingSessionOpening.json")
    private Resource requestWronglyFormattedAgendaIdVotingSessionOpening;

    @Value("classpath:/requestNullDeadlineMinutesVotingSessionOpening.json")
    private Resource requestNullDeadlineMinutesVotingSessionOpening;

    @Value("classpath:/requestNegativeDeadlineMinutesVotingSessionOpening.json")
    private Resource requestNegativeDeadlineMinutesVotingSessionOpening;

    @Value("classpath:/requestZeroDeadlineMinutesVotingSessionOpening.json")
    private Resource requestZeroDeadlineMinutesVotingSessionOpening;

    private String sessionUUID;
    private String agendaUUID;
    private String agendaTitle;
    private Long deadlineMinutes;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private LocalDateTime closingTimeBasedOnDefaultDeadline;
    private String requestFormatErrorCode;
    private String incorrectRequestFormat;
    private String agendaIdField;
    private String deadlineMinutesField;

    private static final Long DEFAULT_DEADLINE_MINUTES = 1L;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.sessionUUID = randomUUID().toString();
        this.agendaUUID = "2b6f8057-cd5e-4a20-afa0-c04419a8983b";
        this.agendaTitle = "Eleição de Diretoria";
        this.deadlineMinutes = 5L;
        this.openingTime = now().withNano(0);
        this.closingTime = now().withNano(0).plusMinutes(deadlineMinutes);
        this.closingTimeBasedOnDefaultDeadline = now().withNano(0).plusMinutes(DEFAULT_DEADLINE_MINUTES);

        this.requestFormatErrorCode = "ERR0100";
        this.incorrectRequestFormat = "Incorrect request format";
        this.agendaIdField = "agendaId";
        this.deadlineMinutesField = "deadlineMinutes";
    }

    @Test
    public void shouldReturnResponseDataAndNotReturnErrorsWhenPerformSuccessVotingSessionOpening() throws Exception {
        final ResultActions result = performSuccessOpening();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformOpeningSessionWithEmptyAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithEmptyAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformOpeningSessionWithNullAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithNullAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithExtraSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithMinorSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithMinorSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithWronglyFormattedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithWronglyFormattedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithNegativeDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithNegativeDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningWithZeroDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithZeroDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseDataAndNotReturnErrorsWhenPerformVotingSessionOpeningWithNullDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = performNullDeadlineMinutesOpeningSession();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningAndCatchNotFoundReferenceException() throws Exception {
        final ResultActions result = tryOpenSessionWithNotFoundException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVotingSessionOpeningAndCatchValidationException() throws Exception {
        final ResultActions result = tryOpenSessionWithValidationException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnOpenedVotingSessionWithGeneratedUUIDAndPeriodTimes() throws Exception {
        final ResultActions result = performSuccessOpening();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sessionUUID))
                .andExpect(jsonPath("$.data.agenda").exists())
                .andExpect(jsonPath("$.data.agenda.id").value(agendaUUID))
                .andExpect(jsonPath("$.data.agenda.title").value(agendaTitle))
                .andExpect(jsonPath("$.data.openingTime").value(openingTime.toString()))
                .andExpect(jsonPath("$.data.closingTime").value(closingTime.toString()));
    }

    @Test
    public void shouldReturnInvalidSizeResponseErrorWhenTryingToPerformVotingSessionOpeningWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithExtraSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("voting.session.agenda.id.invalid.size", "voting.session.agenda.id.invalid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("2b6f8057-cd5e-4a20-afa0-c04419a8983b-f9ksa1s3", "2b6f8057-cd5e-4a20-afa0-c04419a8983b-f9ksa1s3")));
    }

    @Test
    public void shouldReturnInvalidSizeResponseErrorWhenTryingToPerformVotingSessionOpeningWithMinorSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithMinorSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("voting.session.agenda.id.invalid.size", "voting.session.agenda.id.invalid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("2b6f8057-cd5e-4a20-afa0", "2b6f8057-cd5e-4a20-afa0")));
    }

    @Test
    public void shouldReturnNotEmptyAndInvalidSizedResponseErrorWhenTryingToPerformVotingSessionOpeningWithEmptyAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithEmptyAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("voting.session.agenda.id.not.empty", "voting.session.agenda.id.invalid.size", "voting.session.agenda.id.invalid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("", "", "")));
    }

    @Test
    public void shouldReturnInvalidAndNotEmptyResponseErrorWhenTryingToPerformVotingSessionOpeningWithNullAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithNullAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("voting.session.agenda.id.invalid", "voting.session.agenda.id.not.empty")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder(new String[] { null, null })));
    }

    @Test
    public void shouldReturnInvalidFormatResponseErrorWhenTryingToPerformVotingSessionOpeningWithWronglyFormattedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithWronglyFormattedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("voting.session.agenda.id.invalid.format"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(agendaIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value("2b6f8057-cd5e-4a20-afa0c04419a898-3b"));
    }

    @Test
    public void shouldReturnInvalidResponseErrorWhenTryingToPerformVotingSessionOpeningWithNegativeDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithNegativeDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("voting.session.deadline.minutes.invalid"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(deadlineMinutesField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value(-2L));
    }

    @Test
    public void shouldReturnInvalidResponseErrorWhenTryingToPerformVotingSessionOpeningWithZeroDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithZeroDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("voting.session.deadline.minutes.invalid"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(deadlineMinutesField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value(0L));
    }

    @Test
    public void shouldReturnOpenedVotingSessionWithPeriodTimesBasedOnDefaultDeadlineMinutesValue() throws Exception {
        final ResultActions result = performNullDeadlineMinutesOpeningSession();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sessionUUID))
                .andExpect(jsonPath("$.data.agenda").exists())
                .andExpect(jsonPath("$.data.agenda.id").value(agendaUUID))
                .andExpect(jsonPath("$.data.agenda.title").value(agendaTitle))
                .andExpect(jsonPath("$.data.openingTime").value(openingTime.toString()))
                .andExpect(jsonPath("$.data.closingTime").value(closingTimeBasedOnDefaultDeadline.toString()));
    }

    @Test
    public void shouldReturnReferenceNotFoundResponseErrorWhenTryingToPerformVotingSessionOpeningAndCatchNotFoundReferenceException() throws Exception {
        final ResultActions result = tryOpenSessionWithNotFoundException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value("ERR0300"))
                .andExpect(jsonPath("$.errors[0].title").value("Reference not found"))
                .andExpect(jsonPath("$.errors[0].detail").value("voting.agenda.not.found"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value("VotingAgenda"))
                .andExpect(jsonPath("$.errors[0].source.parameter").doesNotExist());
    }

    @Test
    public void shouldReturnInvalidParameterResponseErrorWhenTryingToOpenVotingSessionAndCatchValidationException() throws Exception {
        final ResultActions result = tryOpenSessionWithValidationException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value("ERR0400"))
                .andExpect(jsonPath("$.errors[0].title").value("Invalid parameter"))
                .andExpect(jsonPath("$.errors[0].detail").value("voting.session.already.opened"))
                .andExpect(jsonPath("$.errors[0].source").doesNotExist());
    }

    private ResultActions performSuccessOpening() throws Exception {
        VotingAgenda agenda = new VotingAgenda(agendaUUID, agendaTitle);
        VotingSession votingSession = new VotingSession(sessionUUID, agenda, openingTime, closingTime);
        when(votingSessionService.openFor(agendaUUID, deadlineMinutes)).thenReturn(votingSession);

        final String bodyContent = Resources.toString(requestOpenVotingSession.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithEmptyAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestEmptyAgendaIdVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithNullAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestNullAgendaIdVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithExtraSizedAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestExtraSizedAgendaIdVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithMinorSizedAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestMinorSizedAgendaIdVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithWronglyFormattedAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestWronglyFormattedAgendaIdVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithNegativeDeadlineMinutes() throws Exception {
        final String bodyContent = Resources.toString(requestNegativeDeadlineMinutesVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithZeroDeadlineMinutes() throws Exception {
        final String bodyContent = Resources.toString(requestZeroDeadlineMinutesVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions performNullDeadlineMinutesOpeningSession() throws Exception {
        VotingAgenda agenda = new VotingAgenda(agendaUUID, agendaTitle);
        VotingSession votingSession = new VotingSession(sessionUUID, agenda, openingTime, closingTimeBasedOnDefaultDeadline);
        when(votingSessionService.openFor(agendaUUID, DEFAULT_DEADLINE_MINUTES)).thenReturn(votingSession);

        final String bodyContent = Resources.toString(requestNullDeadlineMinutesVotingSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithNotFoundException() throws Exception {
        when(votingSessionService.openFor(anyString(), anyLong())).thenThrow(new NotFoundReferenceException("VotingAgenda", "voting.agenda.not.found"));

        final String bodyContent = Resources.toString(requestOpenVotingSession.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithValidationException() throws Exception {
        when(votingSessionService.openFor(anyString(), anyLong())).thenThrow(new ValidationException("voting.session.already.opened"));

        final String bodyContent = Resources.toString(requestOpenVotingSession.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

}
