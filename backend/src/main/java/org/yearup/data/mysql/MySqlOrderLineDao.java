package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderLineDAO;
import org.yearup.models.OrderLineItem;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Component
public class MySqlOrderLineDao extends MySqlDaoBase implements OrderLineDAO {

    public MySqlOrderLineDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public OrderLineItem createOrderLine(OrderLineItem orderLineItem) {
        String sql = """
                INSERT INTO order_line_items
                (order_id, product_id, sales_price, quantity, discount)
                VALUES
                (?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, orderLineItem.getOrderId());
            preparedStatement.setInt(2, orderLineItem.getProductId());
            preparedStatement.setBigDecimal(3, orderLineItem.getSalePrice());
            preparedStatement.setInt(4, orderLineItem.getQuantity());
            preparedStatement.setBigDecimal(5, orderLineItem.getDiscount());

            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderLineItemId = generatedKeys.getInt(1);

                    return getById(orderLineItemId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public OrderLineItem getById(int id) {
        String sql = """
                SELECT *
                FROM order_line_items
                WHERE order_line_item_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new OrderLineItem(
                            resultSet.getInt("order_line_item_id"),
                            resultSet.getInt("order_id"),
                            resultSet.getInt("product_id"),
                            resultSet.getBigDecimal("sales_price"),
                            resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("discount")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
