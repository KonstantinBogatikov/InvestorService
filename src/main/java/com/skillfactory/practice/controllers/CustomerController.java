package com.skillfactory.practice.controllers;

import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.service.CustomerService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    // GET /api/customers/{customersId}/balance
    @GetMapping("/{customersId}/balance")
    public ResponseEntity<String> getBalance(@PathVariable("customersId") Long customersId) {
        return service.getBalance(customersId)
                .map(balance -> new ResponseEntity<>(String.format("Баланс пользователя %d: %.2f руб.", customersId, balance),
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND));
    }

    // POST /api/customers/{customersId}/putmoney
    @PostMapping("/{customersId}/putmoney")
    public ResponseEntity<String> putMoney(@PathVariable("customersId") Long customersId, @RequestParam BigDecimal amount) {
        service.putMoney(customersId, amount);
        return new ResponseEntity<>("Пополнение успешно выполнено", HttpStatus.OK);
    }

    // POST /api/customers/{customersId}/takemoney
    @PostMapping("/{customersId}/takemoney")
    public ResponseEntity<String> takeMoney(@PathVariable("customersId") Long customersId, @RequestParam BigDecimal amount) {
        if (service.takeMoney(customersId, amount)) {
            return new ResponseEntity<>("Операция снятия выполнена успешно", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Недостаточно средств на счету", HttpStatus.BAD_REQUEST);
        }
    }

    // GET /api/customers/{customersId}/operations
    @GetMapping("/{customersId}/operations")
    public ResponseEntity<List<Operation>> getOperationList(
            @PathVariable("customersId") Long customersId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to)
            throws BadRequestException {

        LocalDateTime startDate = parseLocalDateTime(from);
        LocalDateTime endDate = parseLocalDateTime(to);

        List<Operation> result = service.getOperationList(customersId, startDate, endDate);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Вспомогательная функция парсинга даты
    private LocalDateTime parseLocalDateTime(String dateStr) throws BadRequestException {
        try {
            if (dateStr != null && !dateStr.trim().isEmpty())
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (Exception e) {
            throw new BadRequestException("Invalid date format");
        }
        return null;
    }

}