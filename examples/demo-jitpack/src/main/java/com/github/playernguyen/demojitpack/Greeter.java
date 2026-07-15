package com.github.playernguyen.demojitpack;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;

@Component
public class Greeter {

    @Inject
    public GreetingService greetingService;

    public String greet(String name) {
        return greetingService.greet(name);
    }
}
