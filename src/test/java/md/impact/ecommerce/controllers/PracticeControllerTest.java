package md.impact.ecommerce.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PracticeController.class)
class PracticeControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void get_returns_fixed_message() throws Exception {
        mvc.perform(get("/api/practice"))
           .andExpect(status().isOk())
           .andExpect(content().string("api initialised"));
    }

    @Test
    void post_echoes_body() throws Exception {
        mvc.perform(post("/api/practice").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Tricou\"}"))
           .andExpect(status().isOk())
           .andExpect(content().string("youve posted: {\"name\":\"Tricou\"}"));
    }

    @Test
    void put_echoes_body() throws Exception {
        mvc.perform(put("/api/practice").contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
           .andExpect(status().isOk())
           .andExpect(content().string("your update is : {\"id\":1}"));
    }

    @Test
    void patch_echoes_body() throws Exception {
        mvc.perform(patch("/api/practice").contentType(MediaType.APPLICATION_JSON).content("{\"price\":9}"))
           .andExpect(status().isOk())
           .andExpect(content().string("you have updated the : {\"price\":9}"));
    }

    @Test
    void delete_echoes_body() throws Exception {
        mvc.perform(delete("/api/practice").contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
           .andExpect(status().isOk())
           .andExpect(content().string("youve deleted : {\"id\":1}"));
    }

    @Test
    void delete_without_body_is_ok() throws Exception {
        mvc.perform(delete("/api/practice"))
           .andExpect(status().isOk())
           .andExpect(content().string("youve deleted : {}"));
    }
}
