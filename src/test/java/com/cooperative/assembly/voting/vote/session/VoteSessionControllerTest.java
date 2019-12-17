package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.error.ResponseErrorHandler;
import com.cooperative.assembly.voting.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.voting.error.exception.ValidationException;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
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
@ContextConfiguration(classes = { VoteSessionController.class, ResponseErrorHandler.class })
public class VoteSessionControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private VoteSessionService voteSessionService;

    @Autowired
    private WebApplicationContext context;

    @Value("classpath:/requestOpenVoteSession.json")
    private Resource requestOpenVoteSession;

    @Value("classpath:/requestEmptyAgendaIdVoteSessionOpening.json")
    private Resource requestEmptyAgendaIdVoteSessionOpening;

    @Value("classpath:/requestNullAgendaIdVoteSessionOpening.json")
    private Resource requestNullAgendaIdVoteSessionOpening;

    @Value("classpath:/requestExtraSizedAgendaIdVoteSessionOpening.json")
    private Resource requestExtraSizedAgendaIdVoteSessionOpening;

    @Value("classpath:/requestMinorSizedAgendaIdVoteSessionOpening.json")
    private Resource requestMinorSizedAgendaIdVoteSessionOpening;

    @Value("classpath:/requestWronglyFormattedAgendaIdVoteSessionOpening.json")
    private Resource requestWronglyFormattedAgendaIdVoteSessionOpening;

    @Value("classpath:/requestNullDeadlineMinutesVoteSessionOpening.json")
    private Resource requestNullDeadlineMinutesVoteSessionOpening;

    @Value("classpath:/requestNegativeDeadlineMinutesVoteSessionOpening.json")
    private Resource requestNegativeDeadlineMinutesVoteSessionOpening;

    @Value("classpath:/requestZeroDeadlineMinutesVoteSessionOpening.json")
    private Resource requestZeroDeadlineMinutesVoteSessionOpening;

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
    public void shouldReturnResponseDataAndNotReturnErrorsWhenPerformSuccessVoteSessionOpening() throws Exception {
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
    public void shouldReturnResponseErrorWhenTryingToPerformVoteSessionOpeningWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithExtraSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVoteSessionOpeningWithMinorSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithMinorSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVoteSessionOpeningWithWronglyFormattedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithWronglyFormattedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVoteSessionOpeningWithNegativeDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithNegativeDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVoteSessionOpeningWithZeroDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithZeroDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseDataAndNotReturnErrorsWhenPerformVoteSessionOpeningWithNullDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = performNullDeadlineMinutesOpeningSession();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVoteSessionOpeningAndCatchNotFoundReferenceException() throws Exception {
        final ResultActions result = tryOpenSessionWithNotFoundException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformVoteSessionOpeningAndCatchValidationException() throws Exception {
        final ResultActions result = tryOpenSessionWithValidationException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void shouldReturnOpenedVoteSessionWithGeneratedUUIDAndPeriodTimes() throws Exception {
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
    public void shouldReturnInvalidSizeResponseErrorWhenTryingToPerformVoteSessionOpeningWithExtraSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithExtraSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.session.agenda.id.invalid.size", "vote.session.agenda.id.invalid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("2b6f8057-cd5e-4a20-afa0-c04419a8983b-f9ksa1s3", "2b6f8057-cd5e-4a20-afa0-c04419a8983b-f9ksa1s3")));
    }

    @Test
    public void shouldReturnInvalidSizeResponseErrorWhenTryingToPerformVoteSessionOpeningWithMinorSizedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithMinorSizedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.session.agenda.id.invalid.size", "vote.session.agenda.id.invalid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("2b6f8057-cd5e-4a20-afa0", "2b6f8057-cd5e-4a20-afa0")));
    }

    @Test
    public void shouldReturnNotEmptyAndInvalidSizedResponseErrorWhenTryingToPerformVoteSessionOpeningWithEmptyAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithEmptyAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.session.agenda.id.not.empty", "vote.session.agenda.id.invalid.size", "vote.session.agenda.id.invalid.format")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder("", "", "")));
    }

    @Test
    public void shouldReturnInvalidAndNotEmptyResponseErrorWhenTryingToPerformVoteSessionOpeningWithNullAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithNullAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("vote.session.agenda.id.invalid", "vote.session.agenda.id.not.empty")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(agendaIdField, agendaIdField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder(new String[] { null, null })));
    }

    @Test
    public void shouldReturnInvalidFormatResponseErrorWhenTryingToPerformVoteSessionOpeningWithWronglyFormattedAgendaIdRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithWronglyFormattedAgendaId();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.session.agenda.id.invalid.format"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(agendaIdField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value("2b6f8057-cd5e-4a20-afa0c04419a898-3b"));
    }

    @Test
    public void shouldReturnInvalidResponseErrorWhenTryingToPerformVoteSessionOpeningWithNegativeDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithNegativeDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.session.deadline.minutes.invalid"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(deadlineMinutesField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value(-2L));
    }

    @Test
    public void shouldReturnInvalidResponseErrorWhenTryingToPerformVoteSessionOpeningWithZeroDeadlineMinutesRequestContentProperty() throws Exception {
        final ResultActions result = tryOpenSessionWithZeroDeadlineMinutes();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.session.deadline.minutes.invalid"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(deadlineMinutesField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value(0L));
    }

    @Test
    public void shouldReturnOpenedVoteSessionWithPeriodTimesBasedOnDefaultDeadlineMinutesValue() throws Exception {
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
    public void shouldReturnReferenceNotFoundResponseErrorWhenTryingToPerformVoteSessionOpeningAndCatchNotFoundReferenceException() throws Exception {
        final ResultActions result = tryOpenSessionWithNotFoundException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value("ERR0300"))
                .andExpect(jsonPath("$.errors[0].title").value("Reference not found"))
                .andExpect(jsonPath("$.errors[0].detail").value("meeting.agenda.not.found"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value("MeetingAgenda"))
                .andExpect(jsonPath("$.errors[0].source.parameter").doesNotExist());
    }

    @Test
    public void shouldReturnInvalidParameterResponseErrorWhenTryingToOpenVoteSessionAndCatchValidationException() throws Exception {
        final ResultActions result = tryOpenSessionWithValidationException();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value("ERR0400"))
                .andExpect(jsonPath("$.errors[0].title").value("Invalid parameter"))
                .andExpect(jsonPath("$.errors[0].detail").value("vote.session.already.opened"))
                .andExpect(jsonPath("$.errors[0].source").doesNotExist());
    }

    private ResultActions performSuccessOpening() throws Exception {
        MeetingAgenda agenda = new MeetingAgenda(agendaUUID, agendaTitle);
        VoteSession voteSession = new VoteSession(sessionUUID, agenda, openingTime, closingTime);
        when(voteSessionService.openFor(agendaUUID, deadlineMinutes)).thenReturn(voteSession);

        final String bodyContent = Resources.toString(requestOpenVoteSession.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithEmptyAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestEmptyAgendaIdVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithNullAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestNullAgendaIdVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithExtraSizedAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestExtraSizedAgendaIdVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithMinorSizedAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestMinorSizedAgendaIdVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithWronglyFormattedAgendaId() throws Exception {
        final String bodyContent = Resources.toString(requestWronglyFormattedAgendaIdVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithNegativeDeadlineMinutes() throws Exception {
        final String bodyContent = Resources.toString(requestNegativeDeadlineMinutesVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithZeroDeadlineMinutes() throws Exception {
        final String bodyContent = Resources.toString(requestZeroDeadlineMinutesVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions performNullDeadlineMinutesOpeningSession() throws Exception {
        MeetingAgenda agenda = new MeetingAgenda(agendaUUID, agendaTitle);
        VoteSession voteSession = new VoteSession(sessionUUID, agenda, openingTime, closingTimeBasedOnDefaultDeadline);
        when(voteSessionService.openFor(agendaUUID, DEFAULT_DEADLINE_MINUTES)).thenReturn(voteSession);

        final String bodyContent = Resources.toString(requestNullDeadlineMinutesVoteSessionOpening.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithNotFoundException() throws Exception {
        when(voteSessionService.openFor(anyString(), anyLong())).thenThrow(new NotFoundReferenceException("MeetingAgenda", "meeting.agenda.not.found"));

        final String bodyContent = Resources.toString(requestOpenVoteSession.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryOpenSessionWithValidationException() throws Exception {
        when(voteSessionService.openFor(anyString(), anyLong())).thenThrow(new ValidationException("vote.session.already.opened"));

        final String bodyContent = Resources.toString(requestOpenVoteSession.getURL(), UTF_8);
        return mockMvc.perform(post("/vote/session/open")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

}
