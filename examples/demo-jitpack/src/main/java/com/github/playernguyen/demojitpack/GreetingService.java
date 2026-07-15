package com.github.playernguyen.demojitpack;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Singleton;

@Component
@Singleton
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "! Powered by MiniInjector via JitPack.";
    }
}
