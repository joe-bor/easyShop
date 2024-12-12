package org.yearup.data;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here

    ShoppingCart addProductToCart(int userId, int productId);

    ShoppingCart updateCart(int userId, int productId, int quantity);

    void clearCart(int userId);
}
