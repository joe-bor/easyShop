package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private ProductDao productDao;

    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        String sql = """
                SELECT *
                FROM shopping_cart
                WHERE user_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);

            // key = productId : ShoppingCarItem
            Map<Integer, ShoppingCartItem> items = new HashMap<>();

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    int quantity = resultSet.getInt("quantity");
                    int product_id = resultSet.getInt("product_id");

                    // get product from db via id
                    Product productFromDB = productDao.getById(product_id);
                    // create shoppingCartItem that has this product
                    ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                    shoppingCartItem.setProduct(productFromDB);

                    // add to hashmap (productId : shoppingCarItem)
                    items.put(product_id, shoppingCartItem);
                }
                // return a populated shopping cart
                ShoppingCart shoppingCart = new ShoppingCart();
                shoppingCart.setItems(items);
                return shoppingCart;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addProductToCart(int userId, int productId, int quantity){
        String sql = """
                INSERT INTO shopping_cart
                (user_id, product_id, quantity)
                VALUES
                (?, ?, ?)
                """;

        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3, quantity);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
