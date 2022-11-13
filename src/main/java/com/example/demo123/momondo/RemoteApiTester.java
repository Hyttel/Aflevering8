package com.example.demo123.momondo;

import com.example.demo123.dto.Gender;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RemoteApiTester implements CommandLineRunner {

    private Mono<String> callSlowEndpoint(){
        Mono<String> slowResponse = WebClient.create()
                .get()
                .uri("http://localhost:8080/random-string-slow")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e-> System.out.println("UUUPS : "+e.getMessage()));
        return slowResponse;
    }

    public void callSlowEndpointBlocking(){
        long start = System.currentTimeMillis();
        List<String> randomString = new ArrayList<>();

        Mono<String> slowResponse = callSlowEndpoint();
        randomString.add(slowResponse.block()); //Three seconds spent


        slowResponse = callSlowEndpoint();
        randomString.add(slowResponse.block());//Three seconds spent

        slowResponse = callSlowEndpoint();
        randomString.add(slowResponse.block());//Three seconds spent
        long end = System.currentTimeMillis();
        randomString. add(0,"Time spent BLOCKING (ms): "+(end-start));

        System.out.println(randomString.stream().collect(Collectors.joining(",")));
    }

    public void callSlowEndpointNonBlocking(){
        long start = System.currentTimeMillis();
        Mono<String> sr1 = callSlowEndpoint();
        Mono<String> sr2 = callSlowEndpoint();
        Mono<String> sr3 = callSlowEndpoint();

        var rs = Mono.zip(sr1,sr2,sr3).map(t-> {
            List<String> randomStrings = new ArrayList<>();
            randomStrings.add(t.getT1());
            randomStrings.add(t.getT2());
            randomStrings.add(t.getT3());
            long end = System.currentTimeMillis();
            randomStrings.add(0,"Time spent NON-BLOCKING (ms): " + (end-start));
            return randomStrings;
        });
        List<String> randoms = rs.block(); //We only block when all the three Mono's has fulfilled
        System.out.println(randoms.stream().collect(Collectors.joining(",")));
    }

    Mono<Gender> getGenderForName(String name) {
        WebClient client = WebClient.create();
        Mono<Gender> gender = client.get()
                .uri("https://api.genderize.io?name="+name)
                .retrieve()
                .bodyToMono(Gender.class);
        return gender;
    }

    String name = "mikkel";
    List<String> names = Arrays.asList("lars", "peter", "sanne", "kim", "david", "maja");

    @Override
    public void run(String... args) throws Exception {
        System.out.println(callSlowEndpoint().toString());

        String randomStr = callSlowEndpoint().block();
        System.out.println(randomStr);

        callSlowEndpointBlocking();
        callSlowEndpointNonBlocking();

        System.out.println(getGenderForName(name));

    }
}
