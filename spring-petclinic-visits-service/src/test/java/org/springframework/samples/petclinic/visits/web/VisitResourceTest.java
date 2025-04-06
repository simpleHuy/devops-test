package org.springframework.samples.petclinic.visits.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitResource.class)
class VisitResourceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VisitRepository visitRepository;

    @Test
    void createVisit_shouldReturnCreatedVisit() throws Exception {
        Visit input = new Visit();
        input.setDate(new Date());
        input.setDescription("General checkup");

        Visit saved = new Visit();
        saved.setId(1);
        saved.setDate(input.getDate());
        saved.setDescription(input.getDescription());
        saved.setPetId(5);

        given(visitRepository.save(any(Visit.class))).willReturn(saved);

        mvc.perform(post("/owners/*/pets/5/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(5))
            .andExpect(jsonPath("$.description").value("General checkup"));

        // Optional: kiểm tra petId được set chính xác
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        assertThat(captor.getValue().getPetId()).isEqualTo(5);
    }

    @Test
    void readVisitByPetId_shouldReturnVisits() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(10)
            .description("Vaccination")
            .date(new Date())
            .build();

        given(visitRepository.findByPetId(10)).willReturn(List.of(visit));

        mvc.perform(get("/owners/*/pets/10/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].description").value("Vaccination"))
            .andExpect(jsonPath("$[0].petId").value(10));
    }

    @Test
    void readVisitByPetId_shouldReturnEmptyListIfNoneFound() throws Exception {
        given(visitRepository.findByPetId(99)).willReturn(Collections.emptyList());

        mvc.perform(get("/owners/*/pets/99/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void readVisitsByMultiplePetIds_shouldReturnVisits() throws Exception {
        Visit visit1 = Visit.VisitBuilder.aVisit().id(1).petId(100).description("Check 1").date(new Date()).build();
        Visit visit2 = Visit.VisitBuilder.aVisit().id(2).petId(200).description("Check 2").date(new Date()).build();

        given(visitRepository.findByPetIdIn(Arrays.asList(100, 200))).willReturn(List.of(visit1, visit2));

        mvc.perform(get("/pets/visits?petId=100,200"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.items[1].id").value(2));
    }

    @Test
    void readVisitsByMultiplePetIds_shouldReturnEmptyListIfNoneFound() throws Exception {
        given(visitRepository.findByPetIdIn(List.of(999))).willReturn(Collections.emptyList());

        mvc.perform(get("/pets/visits?petId=999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(0));
    }
}
