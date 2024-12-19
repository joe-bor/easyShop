package org.yearup.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItem {
    //order_line_item_id PK
    private int orderLineItemId;
    private int orderId;
    private int productId;
    // product_id FK
    private BigDecimal salePrice; // shoppingCartItem.getlineTotal()
    private int quantity; // # of product
    private BigDecimal discount;
}
