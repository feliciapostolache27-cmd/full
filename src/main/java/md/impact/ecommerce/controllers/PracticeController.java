package md.impact.ecommerce.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/practice", produces = MediaType.TEXT_PLAIN_VALUE)
public class PracticeController {

    @GetMapping
    public String get() {
        return "api initialised";
    }

    @PostMapping
    public String post(@RequestBody(required = false) String body) {
        return "youve posted: " + payload(body);
    }

    @PutMapping
    public String put(@RequestBody(required = false) String body) {
        return "your update is : " + payload(body);
    }

    @PatchMapping
    public String patch(@RequestBody(required = false) String body) {
        return "you have updated the : " + payload(body);
    }

    @DeleteMapping
    public String delete(@RequestBody(required = false) String body) {
        return "youve deleted : " + payload(body);
    }

    // Daca nu s-a trimis niciun corp, intoarcem {} ca marcaj vizibil.
    private String payload(String body) {
        return (body == null || body.isBlank()) ? "{}" : body.trim();
    }
}
