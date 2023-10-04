package com.odinbook;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.model.Account;
import com.odinbook.pojo.Message;
import com.odinbook.repository.AccountRepository;
import com.odinbook.service.AccountServiceImpl;
import com.odinbook.service.ImageServiceImpl;
import com.odinbook.validation.AccountForm;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Profile(value = "test")
public class AccountTest {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ImageServiceImpl imageService;
    @Autowired
    private AccountServiceImpl accountService;

    @Value("${spring.cloud.azure.pubsub.connection-string}")
    private String connectStr;
    @BeforeEach
    public void beforeEach() throws IOException{

        Mockito
                .doNothing()
                .when(imageService)
                .createBlob(anyString(),any(MultipartFile.class));
        Mockito
                .when(imageService.findBlob(anyString()))
                .thenReturn("test".getBytes());

        accountRepository.deleteAll();
    }

    @AfterEach
    public void afterEach() {

        accountRepository.deleteAll();
    }

    @Test
    public void findAllAccounts() throws Exception {



        testUtils.createRandomAccount();
        testUtils.createRandomAccount();
        testUtils.createRandomAccount();

        MvcResult mvcResult = mockMvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andReturn();

        List<Account> accountList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(3, accountList.size());

    }


    @Test
    public void createAccount() throws Exception {

        MvcResult mvcResult = mockMvc.perform(
                        post("/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .param("fullName","myFullName")
                                .param("userName","userName")
                                .param("email","exampleuser@gmail.com")
                                .param("password","password")
                                .param("passwordConfirm","password")
                                .param("aboutMe","aboutMe")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();


        Account account = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), Account.class);

        assertEquals("my full name", account.getFullName());
        assertEquals("userName", account.getUserName());
        assertEquals("exampleuser@gmail.com", account.getEmail());
        assertEquals("aboutMe", account.getAboutMe());
        assertNotNull(account.getId());

    }

    @Test
    public void createAccountWithMissingFields() throws Exception {


        AccountForm accountForm = new AccountForm();
        accountForm.setFullName("my full name");
        accountForm.setUserName("userName");
        accountForm.setEmail("exampleuser@gmail.com");
        accountForm.setPassword("password");


        String json = new ObjectMapper().writeValueAsString(accountForm);

        mockMvc.perform(
                        post("/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());



    }


    @Test
    public void getExistingAccountById() throws Exception {


        Account account = testUtils.createRandomAccount();


        MvcResult mvcResult = mockMvc.perform(
                        get("/" + account.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();


        Account resAccount = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), Account.class);

        assertEquals(account.getId(), resAccount.getId());

    }

    @Test
    public void getUnExistingAccount() throws Exception {

        mockMvc.perform(
                        get("/50000")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void updateAccount() throws Exception{

        Account account = testUtils.createRandomAccount();

        MvcResult mvcResult = mockMvc.perform(
                        put("/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .param("id",account.getId().toString())
                                .param("fullName","updateName")
                                .param("userName","updateUser")
                                .param("email",account.getEmail())
                                .param("password","password")
                                .param("passwordConfirm","password")
                                .param("aboutMe","aboutMe")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        Account resAccount = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), Account.class);

        assertEquals(account.getId(),resAccount.getId());
        assertEquals("updateName",resAccount.getFullName());
        assertEquals("updateUser",resAccount.getUserName());

    }



    @Test
    public void ts() throws URISyntaxException, JsonProcessingException, InterruptedException {
//        Account searchAccount1 = testUtils.createRandomAccount();
//        Account searchAccount2 = testUtils.createRandomAccount();
//
//        Mockito
//                .when(accountService.searchAccountsByUserNameOrEmail(anyString()))
//                .thenReturn(List.of(searchAccount1,searchAccount2));
//        Mockito
//                .when(accountService.getClientAccessToken(anyLong()))
//                .thenCallRealMethod();
//

        accountService.getClientAccessToken(0L);

        WebPubSubServiceClient service = new WebPubSubServiceClientBuilder()
                .connectionString(connectStr)
                .hub("accountSearch")
                .buildClient();


        WebPubSubClientAccessToken token = service.getClientAccessToken(
                new GetClientAccessTokenOptions()
                        .setUserId("1")
        );
        WebSocketClient webSocketClient = new WebSocketClient(new URI(token.getUrl())) {
            @Override
            public void onMessage(String message) {
                System.out.printf("Message received: %s%n", message);

            }

            @Override
            public void onClose(int arg0, String arg1, boolean arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onError(Exception arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onOpen(ServerHandshake arg0) {
                // TODO Auto-generated method stub

            }

        };

        webSocketClient.connect();

        Message message = new Message();
        message.setId(1L);
        message.setContent("Hello World");

        String jsonString = new ObjectMapper().writeValueAsString(message);
        service.sendToUser("0",jsonString,WebPubSubContentType.TEXT_PLAIN);
        Thread.sleep(5000L);



    }




}
