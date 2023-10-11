package com.odinbook.accountservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.repository.AccountRepository;
import com.odinbook.accountservice.service.ElasticSearchServiceImpl;
import com.odinbook.accountservice.service.ImageServiceImpl;
import com.odinbook.accountservice.validation.AccountForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountTest {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ImageServiceImpl imageService;
    @MockBean
    private ElasticSearchServiceImpl elasticSearchService;


    @BeforeEach
    public void beforeEach() throws IOException {
        Mockito
                .doNothing()
                .when(imageService)
                .createBlob(Mockito.anyString(),Mockito.any());
        Mockito
                .doNothing()
                .when(elasticSearchService)
                .insertAccount(Mockito.any());

        Mockito
                .doNothing()
                .when(elasticSearchService)
                .updateAccount(Mockito.any());

        Mockito
                .when(elasticSearchService.searchAccountsByUserNameOrEmail(Mockito.anyString()))
                .thenReturn(List.of(
                        testUtils.createRandomAccount(),
                        testUtils.createRandomAccount(),
                        testUtils.createRandomAccount()
                        ));

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

        assertEquals("myFullName", account.getFullName());
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
                                .param("picture",account.getPicture())
                                .param("aboutMe","aboutMe")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        Account resAccount = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), Account.class);

        assertEquals(account.getId(),resAccount.getId());
        assertEquals("updateName",resAccount.getFullName());
        assertEquals("updateUser",resAccount.getUserName());
        assertEquals("picture",resAccount.getPicture());

    }








}
