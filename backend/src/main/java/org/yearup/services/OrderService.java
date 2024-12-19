package org.yearup.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineDAO;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;

import java.util.Collection;

/**
 * Service for handling order-related operations such as checkout and transforming
 * shopping cart items into order line items.
 */
@Service
@AllArgsConstructor
public class OrderService {

    private OrderDao orderDao;
    private OrderLineDAO orderLineDAO;
    private ProfileDao profileDao;
    private ShoppingCartDao shoppingCartDao;
    private EmailService emailService;

    /**
     * Processes the checkout operation for a given user by creating an order
     * and transforming shopping cart items into order line items.
     *
     * @param userId the ID of the user performing the checkout.
     */
    public void checkout(int userId) {
        Profile profile = profileDao.getProfileById(userId);
        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);

        // Create an Order object populated with the user details from Profile && current time && order total
        Order order = this.orderDao.checkout(userId, profile, shoppingCart);

        toOrderLineItems(shoppingCart.getItems().values(), order);

        emailOrderSummary(profile, order.getOrderId(), shoppingCart); // TODO: turn this async, can be called right after we get the order object back

        // empty the cart
        this.shoppingCartDao.clearCart(userId);
    }

    private void emailOrderSummary(Profile profile, int orderId, ShoppingCart shoppingCart) {
        // recipient's email
        String to = profile.getEmail();

        // subject of email
        String subject = "Order Confirmed! Order # " + orderId;

        // email content
        String body = this.emailService.createBody(shoppingCart.getItems().values());

        this.emailService.sendEmail(to, subject, body);
    }

    /**
     * Converts a collection of shopping cart items into order line items
     * and associates them with the specified order.
     *
     * @param shoppingCartItemCollection the collection of shopping cart items to transform.
     * @param order the order to associate the line items with.
     */
    private void toOrderLineItems(Collection<ShoppingCartItem> shoppingCartItemCollection, Order order) {

        for (ShoppingCartItem shoppingCartItem : shoppingCartItemCollection) {
            OrderLineItem orderLineItem = new OrderLineItem();
            orderLineItem.setOrderId(order.getOrderId());
            orderLineItem.setProductId(shoppingCartItem.getProductId());
            orderLineItem.setSalePrice(shoppingCartItem.getLineTotal());
            orderLineItem.setQuantity(shoppingCartItem.getQuantity());
            orderLineItem.setDiscount(shoppingCartItem.getDiscountPercent());

            // Insert to the database
            OrderLineItem orderLineFromDb = this.orderLineDAO.createOrderLine(orderLineItem);
        }
    }

}
