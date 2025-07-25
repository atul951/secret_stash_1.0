package com.secretstash.note.e2e

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.secretstash.note.dto.ErrorResponseDto
import com.secretstash.note.dto.auth.AuthRequestDto
import com.secretstash.note.dto.auth.AuthResponseDto
import com.secretstash.note.dto.auth.UserRegistrationRequestDto
import com.secretstash.note.dto.note.CreateNoteRequestDto
import com.secretstash.note.dto.note.NoteResponseDto
import com.secretstash.note.dto.note.UpdateNoteRequestDto
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NotesE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private var authToken: String = ""
    private var noteId: Long = 0L

    @Test
    @Order(0)
    fun `register first user and fetch it's auth token post login`() {
        // GIVEN - register and login request
        val registerUserRequest = UserRegistrationRequestDto(
            username = "test_user",
            email = "atul@example.com",
            password = "password@123"
        )
        val loginUserRequest = AuthRequestDto(
            username = registerUserRequest.username,
            password = registerUserRequest.password
        )

        // WHEN - call user register and login apis
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerUserRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginUserRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.token", Matchers.notNullValue()) }
            .andExpect { jsonPath("$.refreshToken", Matchers.notNullValue()) }
            .andReturn()
            .response
            .contentAsString

        // THEN - fetch token
        val authResponse = objectMapper.readValue(response, AuthResponseDto::class.java)

        // Assign the token to global variable
        this.authToken = authResponse.token
    }

    @Test
    @Order(1)
    fun `when given valid request, should create user note successfully`() {
        // GIVEN - Note creation request
        val noteCreationRequest = CreateNoteRequestDto(
            title = "First Note",
            content = "This is my first note expiring in 1 day.",
            expiresAt = LocalDateTime.now().plusDays(1)
        )

        // WHEN - call note creation api
        val response = mockMvc.post("/api/notes") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(noteCreationRequest)
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isCreated() } }
            .andReturn()
            .response
            .contentAsString

        // get the note
        val noteResponse = objectMapper.readValue(response, NoteResponseDto::class.java)
        this.noteId = noteResponse.id

        // THEN - assert
        assertEquals(noteCreationRequest.title, noteResponse.title)
        assertEquals(noteCreationRequest.content, noteResponse.content)
        assertEquals(noteCreationRequest.expiresAt, noteResponse.expiresAt)
    }

    @Test
    @Order(1)
    fun `when given invalid request, should return error and not create user note`() {
        // GIVEN - Note creation request
        val noteCreationRequest = CreateNoteRequestDto(
            title = "Second Note",
            content = "This is my second note expired 1 day ago.",
            expiresAt = LocalDateTime.now().minusDays(1)
        )

        // WHEN - call note creation api
        val response = mockMvc.post("/api/notes") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(noteCreationRequest)
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response
            .contentAsString

        // get the error response
        val errorResponse = objectMapper.readValue(response, ErrorResponseDto::class.java)

        // THEN - assert
        assertEquals(400, errorResponse.status)
        assertEquals("Validation Error", errorResponse.error)
        assertEquals("expiresAt: Expiration date and time must be in the future.", errorResponse.message)
    }

    @Test
    @Order(2)
    fun `when given valid request, should update user note successfully`() {
        // GIVEN - Note update request
        val noteUpdateRequest = UpdateNoteRequestDto(
            title = "First Note [Updated]",
            content = "This is my updated first note, now expiring in 2 days.",
            expiresAt = LocalDateTime.now().plusDays(2)
        )

        // WHEN - call note creation api
        val response = mockMvc.put("/api/notes/${this.noteId}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(noteUpdateRequest)
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // get the note
        val noteResponse = objectMapper.readValue(response, NoteResponseDto::class.java)

        // THEN - assert
        assertEquals(noteUpdateRequest.title, noteResponse.title)
        assertEquals(noteUpdateRequest.content, noteResponse.content)
        assertEquals(noteUpdateRequest.expiresAt, noteResponse.expiresAt)
    }

    @Test
    @Order(2)
    fun `when given invalid request, should return error and not update user note`() {
        // GIVEN - Note update request
        val noteUpdateRequest = UpdateNoteRequestDto(
            title = "First Note [Updated]",
            content = "This is my updated first note expired 2 days ago.",
            expiresAt = LocalDateTime.now().minusDays(2)
        )

        // WHEN - call note creation api
        val response = mockMvc.put("/api/notes/${this.noteId}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(noteUpdateRequest)
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response
            .contentAsString

        // get the error response
        val errorResponse = objectMapper.readValue(response, ErrorResponseDto::class.java)

        // THEN - assert
        assertEquals(400, errorResponse.status)
        assertEquals("Validation Error", errorResponse.error)
        assertEquals("expiresAt: Expiration date and time must be in the future.", errorResponse.message)
    }

    @Test
    @Order(3)
    fun `when given valid note id, should fetch user note successfully`() {
        // GIVEN - Note id from Test #2
        // WHEN - call note fetching api
        val response = mockMvc.get("/api/notes/${this.noteId}") {
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // get the note
        val noteResponse = objectMapper.readValue(response, NoteResponseDto::class.java)

        // THEN - assert
        assertEquals(noteId, noteResponse.id)
        assertNotNull(noteResponse.title)
        assertNotNull(noteResponse.content)
        assertNotNull(noteResponse.expiresAt)
    }

    @Test
    @Order(4)
    fun `when given valid note id, should delete user note successfully`() {
        // GIVEN - Note id from Test #2
        // WHEN - call note deletion api
        mockMvc.delete("/api/notes/${this.noteId}") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            // THEN - assert
            .andExpect { status { isNoContent() } }
    }

    @Test
    @Order(5)
    fun `when given deleted note id, should return error and not delete user note`() {
        // GIVEN - Note id from Test #4
        // WHEN - call note deletion api
        val response = mockMvc.delete("/api/notes/${this.noteId}") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            // THEN - assert
            .andExpect { status { isNotFound() } }
            .andReturn()
            .response
            .contentAsString

        // get the error response
        val errorResponse = objectMapper.readValue(response, ErrorResponseDto::class.java)

        // THEN - assert
        assertEquals(404, errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertEquals("Note not found with ID=$noteId.", errorResponse.message)
    }

    @Test
    @Order(6)
    fun `when requesting all notes, should fetch active user notes successfully`() {
        // GIVEN - Note creation requests
        val count = 5
        val noteCreationRequests = mutableListOf<CreateNoteRequestDto>()
        // active notes
        for (i in 1..count) {
            noteCreationRequests.add(
                CreateNoteRequestDto(
                    title = "Note #$i",
                    content = "This is my note expiring in $i day(s).",
                    expiresAt = LocalDateTime.now().plusDays(i.toLong())
                )
            )
        }
        // expired notes
        for (i in 1..count) {
            noteCreationRequests.add(
                CreateNoteRequestDto(
                    title = "Note #-$i",
                    content = "This is my note which was expired few second(s) ago.",
                    expiresAt = LocalDateTime.now().plusSeconds(5)
                )
            )
        }

        // call note creation api
        for (i in 0 until count + count) {
            mockMvc.post("/api/notes") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(noteCreationRequests[i])
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
            }
                .andExpect { status { isCreated() } }
        }

        // let some notes be expired
        Thread.sleep(7000)

        // WHEN - call get active user notes api
        val response = mockMvc.get("/api/notes") {
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // get the user notes
        val noteResponseList = objectMapper.readValue(response, object : TypeReference<List<NoteResponseDto>>() {})

        // THEN - assert
        assertEquals(count, noteResponseList.size)
        for (i in 0 until count) {
            assertEquals(noteCreationRequests[i].title, noteResponseList[i].title)
            assertEquals(noteCreationRequests[i].content, noteResponseList[i].content)
            assertEquals(noteCreationRequests[i].expiresAt, noteResponseList[i].expiresAt)
        }
    }

    @Test
    @Order(7)
    fun `when given expired note id, should return error and not fetch user note`() {
        // GIVEN - Expired note id from Test #6
        val expiredNoteId = 7L

        // WHEN - call note fetching api
        val response = mockMvc.get("/api/notes/$expiredNoteId") {
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response
            .contentAsString

        // get the error response
        val errorResponse = objectMapper.readValue(response, ErrorResponseDto::class.java)

        // THEN - assert
        assertEquals(400, errorResponse.status)
        assertEquals("Bad Request", errorResponse.error)
        assertEquals("Note '$expiredNoteId' has already expired.", errorResponse.message)
    }

    @Test
    @Order(7)
    fun `when given deleted note id, should return error and not fetch user note`() {
        // GIVEN - Deleted note id from Test #4
        // WHEN - call note fetching api
        val response = mockMvc.get("/api/notes/$noteId") {
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isNotFound() } }
            .andReturn()
            .response
            .contentAsString

        // get the error response
        val errorResponse = objectMapper.readValue(response, ErrorResponseDto::class.java)

        // THEN - assert
        assertEquals(404, errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertEquals("Note not found with ID=$noteId.", errorResponse.message)
    }

    @Test
    @Order(8)
    fun `when requesting latest notes, should fetch latest active user notes successfully`() {
        // GIVEN - Active user notes from Test #6
        val count = 5

        // WHEN - call get latest active user notes api
        val response = mockMvc.get("/api/notes/latest") {
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // get the user notes
        val noteResponseList = objectMapper.readValue(response, object : TypeReference<List<NoteResponseDto>>() {})

        // THEN - assert
        assertEquals(count, noteResponseList.size)
        val sortedNoteResponseList = noteResponseList.sortedByDescending { it.createdAt }
        assertEquals(sortedNoteResponseList, noteResponseList)
    }

    @Test
    @Order(9)
    fun `register second user and fetch it's auth token post login`() {
        // GIVEN - register and login request
        val registerUserRequest = UserRegistrationRequestDto(
            username = "test_user_2",
            email = "kumar@example.com",
            password = "password@321"
        )
        val loginUserRequest = AuthRequestDto(
            username = registerUserRequest.username,
            password = registerUserRequest.password
        )

        // WHEN - call user register and login apis
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerUserRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginUserRequest)
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // THEN - fetch token
        val authResponse = objectMapper.readValue(response, AuthResponseDto::class.java)

        // Assign the token to global variable
        this.authToken = authResponse.token
    }

    @Test
    @Order(10)
    fun `when requesting all notes for second user, should fetch active user notes successfully`() {
        // GIVEN - Note creation requests
        val count = 10
        val noteCreationRequests = mutableListOf<CreateNoteRequestDto>()
        // active notes
        for (i in 1..count) {
            noteCreationRequests.add(
                CreateNoteRequestDto(
                    title = "Kumar's Note #$i",
                    content = "This is my note expiring in $i day(s).",
                    expiresAt = LocalDateTime.now().plusDays(i.toLong())
                )
            )
        }

        // call note creation api
        for (i in 0 until count) {
            mockMvc.post("/api/notes") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(noteCreationRequests[i])
                accept = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
            }
                .andExpect { status { isCreated() } }
        }

        // WHEN - call get active user notes api
        val response = mockMvc.get("/api/notes") {
            accept = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        // get the user notes
        val noteResponseList = objectMapper.readValue(response, object : TypeReference<List<NoteResponseDto>>() {})

        // THEN - assert
        assertEquals(count, noteResponseList.size)
        for (i in 0 until count) {
            assertEquals(noteCreationRequests[i].title, noteResponseList[i].title)
            assertEquals(noteCreationRequests[i].content, noteResponseList[i].content)
            assertEquals(noteCreationRequests[i].expiresAt, noteResponseList[i].expiresAt)
        }
    }
}
