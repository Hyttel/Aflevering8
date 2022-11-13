package com.example.demo123.Controllers;

import com.example.demo123.dto.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class NameController {

    @RequestMapping("/name-info")
    public NameResponse getDetails(@RequestParam String name) {

        Mono<Age> age = getAge(name); // lav en getAge(String name)
        Mono<Gender> gender = getGender(name);  // lav en getGender(name)
        Mono<Nationality> nationality = getNationality(name); // lav en getNationality (String name)

        var resMono  = Mono.zip(age, gender, nationality).map(t -> {
            NameResponse ns = new NameResponse();

            ns.setAge(t.getT1().getAge());
            ns.setAgeCount(t.getT1().getCount());

            ns.setGender(t.getT2().getGender());
            ns.setGenderProbability(t.getT2().getProbability());

            ns.setCountry(t.getT3().getCountry().get(0).getCountry_id());
            ns.setCountryProbability(t.getT3().getCountry().get(0).getProbability());

            return ns;
        });

        NameResponse res = resMono.block();
        res.setName(name);
        // Call all setters

        return res;
    }

    //Getters lavet efter undervisning
    public Mono<Age> getAge(String name){
        WebClient client = WebClient.create();
        Mono<Age> age = client.get().uri("https://api.agify.io?name=" + name).retrieve().bodyToMono(Age.class);
        return age;
    }

    public Mono<Gender> getGender(String name){
        WebClient client = WebClient.create();
        Mono<Gender> gender = client.get().uri("https://api.genderize.io?name=" + name).retrieve().bodyToMono(Gender.class);
        return gender;
    }

    public Mono<Nationality> getNationality(String name){
        WebClient client = WebClient.create();

        Mono<Nationality> nationality = client.get().uri("https://api.nationalize.io?name=" + name).retrieve().bodyToMono(Nationality.class);
        return nationality;
    }


}
