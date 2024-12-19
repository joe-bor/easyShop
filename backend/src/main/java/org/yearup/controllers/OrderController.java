package org.yearup.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineDAO;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;
import org.yearup.services.EmailService;
import org.yearup.services.OrderService;
import org.yearup.utils.LoggedInUser;

import java.security.Principal;

@RestController
@RequestMapping("orders")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@CrossOrigin
public class OrderController {

    private LoggedInUser loggedInUser;
    private OrderService orderService;

    @PostMapping
    public void checkout(Principal principal) {
        int userId = this.loggedInUser.getUserId(principal);
        this.orderService.checkout(userId);
    }
}
