package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ChartOfAccountDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.account.ChartOfAccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class ChartOfAccountController {

    private final ChartOfAccountService service;

    @GetMapping
    public ResponseEntity<List<ChartOfAccountDTO>> getAccounts(@RequestParam(required = false) String group) {
        if (group != null && !group.trim().isEmpty()) {
            return ResponseEntity.ok(service.getByGroup(group));
        }
        return ResponseEntity.ok(service.getAll());
    }
}
