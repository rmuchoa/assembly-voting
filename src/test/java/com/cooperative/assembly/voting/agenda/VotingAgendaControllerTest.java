package com.cooperative.assembly.voting.agenda;

import com.cooperative.assembly.error.ResponseErrorHandler;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@EnableSpringDataWebSupport
@ContextConfiguration(classes = { VotingAgendaController.class, ResponseErrorHandler.class })
public class VotingAgendaControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private VotingAgendaService votingAgendaService;

    @Autowired
    private WebApplicationContext context;

    @Value("classpath:/requestCreateVotingAgenda.json")
    private Resource requestCreateVotingAgenda;

    @Value("classpath:/requestNullTitleVotingAgendaCreation.json")
    private Resource requestNullTitleVotingAgendaCreation;

    @Value("classpath:/requestEmptyTitleVotingAgendaCreation.json")
    private Resource requestEmptyTitleVotingAgendaCreation;

    @Value("classpath:/requestExtraSizedTitleVotingAgendaCreation.json")
    private Resource requestExtraSizedTitleVotingAgendaCreation;

    private String agendaUUID;
    private String agendaTitle;
    private String requestFormatErrorCode;
    private String incorrectRequestFormat;
    private String titleField;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.agendaUUID = randomUUID().toString();
        this.agendaTitle = "Eleição de Diretoria";
        this.requestFormatErrorCode = "ERR0100";
        this.incorrectRequestFormat = "Incorrect request format";
        this.titleField = "title";
    }

    @Test
    public void shouldReturnResponseDataWhenPerformSuccessVotingAgendaCreation() throws Exception {
        final ResultActions result = performSuccessCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void shouldNotReturnResponseErrorsWhenPerformSuccessVotingAgendaCreation() throws Exception {
        final ResultActions result = performSuccessCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnCreatedVotingAgendaWithGeneratedUUID() throws Exception {
        final ResultActions result = performSuccessCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(agendaUUID))
                .andExpect(jsonPath("$.data.title").value(agendaTitle));
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformCreationWithEmptyTitleRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformEmptyPropertyCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformCreationWithNullTitleRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullPropertyCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void shouldReturnResponseErrorWhenTryingToPerformCreationWithExtraSizedTitleRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformExtraSizedPropertyCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void shouldReturnNotEmptyResponseErrorWhenTryingToPerformCreationWithEmptyTitleRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformEmptyPropertyCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("voting.agenda.title.not.empty"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(titleField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value(""));
    }

    @Test
    public void shouldReturnInvalidAndNotEmptyResponseErrorWhenTryingToPerformCreationWithNullTitleRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformNullPropertyCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*]").exists())
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder(requestFormatErrorCode, requestFormatErrorCode)))
                .andExpect(jsonPath("$.errors[*].title", containsInAnyOrder(incorrectRequestFormat, incorrectRequestFormat)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("voting.agenda.title.not.empty", "voting.agenda.title.invalid")))
                .andExpect(jsonPath("$.errors[*].source").exists())
                .andExpect(jsonPath("$.errors[*].source.pointer", containsInAnyOrder(titleField, titleField)))
                .andExpect(jsonPath("$.errors[*].source.parameter", containsInAnyOrder(new String[] { null, null })));
    }

    @Test
    public void shouldReturnInvalidSizeResponseErrorWhenTryingToPerformCreationWithExtraSizedTitleRequestContentProperty() throws Exception {
        final ResultActions result = tryPerformExtraSizedPropertyCreation();

        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").exists())
                .andExpect(jsonPath("$.errors[0].code").value(requestFormatErrorCode))
                .andExpect(jsonPath("$.errors[0].title").value(incorrectRequestFormat))
                .andExpect(jsonPath("$.errors[0].detail").value("voting.agenda.title.invalid.size"))
                .andExpect(jsonPath("$.errors[0].source").exists())
                .andExpect(jsonPath("$.errors[0].source.pointer").value(titleField))
                .andExpect(jsonPath("$.errors[0].source.parameter").value("012345679|0123456789|0123456789|0123456789|0123456789|0123456789|0123456789|0123456789|0123456789|0123456789"));
    }

    private ResultActions performSuccessCreation() throws Exception {
        VotingAgenda votingAgenda = new VotingAgenda(agendaUUID, agendaTitle);
        when(votingAgendaService.create(agendaTitle)).thenReturn(votingAgenda);

        final String bodyContent = Resources.toString(requestCreateVotingAgenda.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/agenda/create")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformEmptyPropertyCreation() throws Exception {
        final String bodyContent = Resources.toString(requestEmptyTitleVotingAgendaCreation.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/agenda/create")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformNullPropertyCreation() throws Exception {
        final String bodyContent = Resources.toString(requestNullTitleVotingAgendaCreation.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/agenda/create")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

    private ResultActions tryPerformExtraSizedPropertyCreation() throws Exception {
        final String bodyContent = Resources.toString(requestExtraSizedTitleVotingAgendaCreation.getURL(), UTF_8);
        return mockMvc.perform(post("/voting/agenda/create")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(bodyContent));
    }

}
