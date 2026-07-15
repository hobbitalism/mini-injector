package com.github.playernguyen.demojitpack;

import com.github.playernguyen.runtime.ContainerBuilder;
import com.github.playernguyen.runtime.InjectionContainer;

public class DemoJitPack {

    public static void main(String[] args) {
        System.out.println("=== MiniInjector via JitPack ===\n");

        InjectionContainer container = ContainerBuilder.create()
            .scanPackage("com.github.playernguyen.demojitpack")
            .build();
        System.out.println("Container built\n");

        Greeter greeter = (Greeter) container.get(Greeter.class.getName());
        System.out.println(greeter.greet("MiniInjector"));
    }
}
