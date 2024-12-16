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
import org.yearup.utils.LoggedInUser;

import java.security.Principal;

@RestController
@RequestMapping("orders")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@CrossOrigin
public class OrderController {

    private LoggedInUser loggedInUser;
    OrderDao orderDao;
    OrderLineDAO orderLineDAO;
    ProfileDao profileDao;
    ShoppingCartDao shoppingCartDao;

    @PostMapping
    public void checkout(Principal principal){
        // using the user id
            // fetch user profile
            // fetch the cart
                // transform cart items to order items
            // transform cart to order: by appending details from profile
        int userId = this.loggedInUser.getUserId(principal);
        Profile profile = profileDao.getProfileById(userId);
        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);

        // Order object populated with the user details from Profile && current time && order total
        Order order = this.orderDao.checkout(userId, profile, shoppingCart);

        // transform cart items to order items and populate associated table in db
        for (ShoppingCartItem shoppingCartItem : shoppingCart.getItems().values()) {
            OrderLineItem orderLineItem = new OrderLineItem();
            orderLineItem.setOrderId(order.getOrderId());
            orderLineItem.setProductId(shoppingCartItem.getProductId());
            orderLineItem.setSalePrice(shoppingCartItem.getLineTotal());
            orderLineItem.setQuantity(shoppingCartItem.getQuantity());
            orderLineItem.setDiscount(shoppingCartItem.getDiscountPercent());

            // insert to db
            OrderLineItem orderLineFromDb = this.orderLineDAO.createOrderLine(orderLineItem);
        }

        this.shoppingCartDao.clearCart(userId);
    }
}
