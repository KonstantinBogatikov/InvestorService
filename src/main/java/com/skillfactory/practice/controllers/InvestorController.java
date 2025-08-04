package com.skillfactory.practice.controllers;

import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.service.InvestorService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/investors")
public class InvestorController {

    private final InvestorService service;

    public InvestorController(InvestorService service) {
        this.service = service;
    }

    // GET /api/investors/{investorId}/balance
    @GetMapping("/{investorId}/balance")
    public ResponseEntity<String> getBalance(@PathVariable("investorId") Long investorId) {
        return service.getBalance(investorId)
                .map(balance -> new ResponseEntity<>(String.format("Баланс пользователя %d: %.2f руб.", investorId, balance),
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND));
    }

    // POST /api/investors/{investorId}/putmoney
    @PostMapping("/{investorId}/putmoney")
    public ResponseEntity<String> putMoney(@PathVariable("investorId") Long investorId, @RequestParam BigDecimal amount) {
        service.putMoney(investorId, amount);
        return new ResponseEntity<>("Пополнение успешно выполнено", HttpStatus.OK);
    }

    // POST /api/investors/{investorId}/takemoney
    @PostMapping("/{investorId}/takemoney")
    public ResponseEntity<String> takeMoney(@PathVariable("investorId") Long investorId, @RequestParam BigDecimal amount) {
        if (service.takeMoney(investorId, amount)) {
            return new ResponseEntity<>("Операция снятия выполнена успешно", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Недостаточно средств на счету", HttpStatus.BAD_REQUEST);
        }
    }

    // GET /api/investors/{investorId}/operations
    @GetMapping("/{investorId}/operations")
    public ResponseEntity<List<Operation>> getOperationList(
            @PathVariable("investorId") Long investorId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to)
            throws BadRequestException {

        LocalDateTime startDate = parseLocalDateTime(from);
        LocalDateTime endDate = parseLocalDateTime(to);

        List<Operation> result = service.getOperationList(investorId, startDate, endDate);
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