package com.skillfactory.practice.integration;

import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Тестируем endpoint возврата баланса для клиента с id = 1
    @Test
    void testReturnBalance() throws Exception {
        Long customerId = 1L;
        BigDecimal balance = new BigDecimal("1000.50");
        when(customerService.getBalance(customerId)).thenReturn(Optional.of(balance));

        mockMvc.perform(get("/api/customers/{customersId}/balance", customerId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Баланс клиента 1: 1000,50 руб.")));

        verify(customerService).getBalance(customerId);
    }

    // Тестируем возврат сообщения, что клиент не найден, когда клиента нет
    @Test
    void testGetBalanceWhenCustomerNotExists() throws Exception {
        Long customerId = 999L;
        when(customerService.getBalance(customerId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/{customersId}/balance", customerId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Клиент не найден"));

        verify(customerService).getBalance(customerId);
    }


    @Test
    void testPutMoney() throws Exception {
        Long customerId = 1L;
        BigDecimal amount = new BigDecimal("500.00");

        mockMvc.perform(post("/api/customers/{customersId}/putmoney", customerId)
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Пополнение успешно выполнено"));

        verify(customerService).putMoney(customerId, amount);
    }

    // Тестируем endpoint списания средств со счета клиента с id = 1
    @Test
    void testTakeMoneyWhenSufficientFunds() throws Exception {
        Long customerId = 1L;
        BigDecimal amount = new BigDecimal("200.00");
        when(customerService.takeMoney(customerId, amount)).thenReturn(true);

        mockMvc.perform(post("/api/customers/{customersId}/takemoney", customerId)
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Операция снятия выполнена успешно"));

        verify(customerService).takeMoney(customerId, amount);
    }

    // Тестируем endpoint списания средств со счета клиента с id = 1, когда денег не достаточно
    @Test
    void testTakeMoneyWhenInsufficientFunds() throws Exception {
        Long customerId = 1L;
        BigDecimal amount = new BigDecimal("2000.00");
        when(customerService.takeMoney(customerId, amount)).thenReturn(false);

        mockMvc.perform(post("/api/customers/{customersId}/takemoney", customerId)
                        .param("amount", amount.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Недостаточно средств на счете"));

        verify(customerService).takeMoney(customerId, amount);
    }

    // Тестируем endpoint получения всех операций для клиента с id = 1
    @Test
    void testReturnAllOperations() throws Exception {
        Long customerId = 1L;
        Operation operation = new Operation(/* инициализация операции */);
        when(customerService.getOperationList(eq(customerId), isNull(), isNull()))
                .thenReturn(List.of(operation));

        mockMvc.perform(get("/api/customers/{customersId}/operations", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(customerService).getOperationList(eq(customerId), isNull(), isNull());
    }

    // Тестируем endpoint получения всех операций для клиента с id = 1 с учетом дат
    @Test
    void testReturnFilteredOperations() throws Exception {
        Long customerId = 1L;
        String from = "2023-01-01T00:00:00";
        String to = "2023-01-31T23:59:59";
        LocalDateTime fromDate = LocalDateTime.parse(from, formatter);
        LocalDateTime toDate = LocalDateTime.parse(to, formatter);

        Operation operation = new Operation();
        when(customerService.getOperationList(eq(customerId), eq(fromDate), eq(toDate)))
                .thenReturn(List.of(operation));

        mockMvc.perform(get("/api/customers/{customersId}/operations", customerId)
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(customerService).getOperationList(eq(customerId), eq(fromDate), eq(toDate));
    }

    // Тестируем endpoint перевода денег
    @Test
    void testTransferMoney() throws Exception {
        Long senderId = 1L;
        Long recipientId = 2L;
        BigDecimal amount = new BigDecimal("300.00");
        when(customerService.transferMoney(senderId, recipientId, amount)).thenReturn(true);

        mockMvc.perform(post("/api/customers/{senderId}/transfermoney/{recipientId}", senderId, recipientId)
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод выполнен успешно"));

        verify(customerService).transferMoney(senderId, recipientId, amount);
    }

    // Тестируем endpoint перевода денег, когда денег недостаточно
    @Test
    void transferMoney_WhenFailed_ShouldReturnErrorMessage() throws Exception {
        Long senderId = 1L;
        Long recipientId = 2L;
        BigDecimal amount = new BigDecimal("3000.00");
        when(customerService.transferMoney(senderId, recipientId, amount)).thenReturn(false);

        mockMvc.perform(post("/api/customers/{senderId}/transfermoney/{recipientId}", senderId, recipientId)
                        .param("amount", amount.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ошибка перевода: недостаточно средств или клиент не найден"));

        verify(customerService).transferMoney(senderId, recipientId, amount);
    }
}