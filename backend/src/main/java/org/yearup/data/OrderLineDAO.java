package org.yearup.data;

import org.yearup.models.OrderLineItem;

public interface OrderLineDAO {
    OrderLineItem createOrderLine(OrderLineItem orderLineItem);
    OrderLineItem getById(int id);
}
