package org.code3.bottin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan
@SpringBootApplication
public class App {

    public static void main(String[] args) throws Exception {
      SpringApplication.run(App.class, args)
    }
}
