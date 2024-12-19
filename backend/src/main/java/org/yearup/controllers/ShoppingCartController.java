package org.yearup.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;
import org.yearup.utils.LoggedInUser;

import java.security.Principal;

@AllArgsConstructor
@RestController
@RequestMapping("cart")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@CrossOrigin
public class ShoppingCartController {
    private ShoppingCartDao shoppingCartDao;
    private LoggedInUser loggedInUser;
    private UserDao userDao;


    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        try {
            int userId = this.loggedInUser.getUserId(principal);

            // use the shoppingcartDao to get all items in the cart and return the cart
            return this.shoppingCartDao.getByUserId(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping("products/{id}")
    public ResponseEntity<ShoppingCart> addProductToCart(@PathVariable int id, Principal principal) {
        try {
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            ShoppingCart shoppingCart = getCart(principal);

            if (shoppingCart.contains(id)) {
                ShoppingCartItem shoppingCartItem = shoppingCart.getItems().get(id);
                int productId = shoppingCartItem.getProductId();
                int quantity = shoppingCartItem.getQuantity();

                shoppingCart = this.shoppingCartDao.updateCart(userId, productId, ++quantity);

            } else {
                shoppingCart =
                        this.shoppingCartDao.addProductToCart(userId, id);
            }
            return ResponseEntity.ok(shoppingCart);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("products/{id}")
    public ResponseEntity<ShoppingCart> updateProductQty(@PathVariable int id, Principal principal, @RequestBody ShoppingCartItem shoppingCartItem) {
        try {
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            ShoppingCart shoppingCart = this.shoppingCartDao.updateCart(userId, id, shoppingCartItem.getQuantity());
            return ResponseEntity.ok(shoppingCart);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping
    public ResponseEntity<ShoppingCart> clearCart(Principal principal) {
        try {
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            ShoppingCart shoppingCart = this.shoppingCartDao.clearCart(userId);
            return ResponseEntity.ok(shoppingCart);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

}
