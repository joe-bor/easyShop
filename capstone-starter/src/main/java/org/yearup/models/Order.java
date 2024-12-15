package org.yearup.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private int orderId;
    private int userId;
    private LocalDate localDate;
    private String address;
    private String city;
    private String state;
    private String zip;
    private double total;
}
