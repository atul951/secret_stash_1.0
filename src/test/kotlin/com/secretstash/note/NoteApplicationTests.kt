package com.secretstash.note

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class NoteApplicationTests {

    @Test
    fun contextLoads() {
        // app ran successfully
    }
}
