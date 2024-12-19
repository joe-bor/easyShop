package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

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

            ShoppingCart shoppingCart = new ShoppingCart();

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    int quantity = resultSet.getInt("quantity");
                    int product_id = resultSet.getInt("product_id");

                    // get product from db via id
                    Product productFromDB = productDao.getById(product_id);
                    // create shoppingCartItem that has this product
                    ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                    shoppingCartItem.setProduct(productFromDB);
                    shoppingCartItem.setQuantity(quantity);

                    // add to hashmap (productId : shoppingCarItem)
                    shoppingCart.add(shoppingCartItem);
                }
                // return a populated shopping cart
                return shoppingCart;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShoppingCart addProductToCart(int userId, int productId) {
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
            preparedStatement.setInt(3, 1);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return this.getByUserId(userId);
    }

//    private void incrementProductQuantity(int userId, int productId) {
//        String sql = """
//                SELECT *
//                FROM shopping_cart
//                WHERE user_id = ?
//                AND product_id = ?
//                """;
//
//        try (Connection connection = getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            preparedStatement.setInt(1, userId);
//            preparedStatement.setInt(2, productId);
//
//            try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                if (resultSet.next()) {
//
//                    int quantity = resultSet.getInt("quantity");
//                    // increment qty
//                    quantity++;
//                    // update table with updated qty
//                    updateCart(userId, productId, quantity);
//                }
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void addProduct(int userId, int productId) {
//        String sql = """
//                INSERT INTO shopping_cart
//                (user_id, product_id, quantity)
//                VALUES
//                (?, ?, ?)
//                """;
//
//        try (Connection connection = getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            preparedStatement.setInt(1, userId);
//            preparedStatement.setInt(2, productId);
//            preparedStatement.setInt(3, 1);
//
//            preparedStatement.executeUpdate();
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private boolean checkIfProductAlreadyInCart(int userId, int productId) {
        String sql = """
                SELECT COUNT(*)
                FROM shopping_cart
                WHERE user_id = ?
                AND product_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt("count") > 0) {
                    // product is already present
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShoppingCart updateCart(int userId, int productId, int quantity) {
        String sql = """
                UPDATE shopping_cart
                SET quantity = ?
                WHERE user_id = ?
                AND product_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, productId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return this.getByUserId(userId);
    }

    @Override
    public ShoppingCart clearCart(int userId) {
        String sql = """
                DELETE FROM shopping_cart
                WHERE user_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return this.getByUserId(userId);
    }
}
