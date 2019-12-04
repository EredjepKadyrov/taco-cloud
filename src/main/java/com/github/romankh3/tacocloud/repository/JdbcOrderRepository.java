package com.github.romankh3.tacocloud.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.romankh3.tacocloud.Order;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOrderRepository implements OrderRepository {

    private final SimpleJdbcInsert orderInserter;
    private final SimpleJdbcInsert orderTacoInserter;
    private final ObjectMapper objectMapper;

    @Autowired
    public JdbcOrderRepository(JdbcTemplate jdbc) {
        this.orderInserter = new SimpleJdbcInsert(jdbc)
                .withTableName("Taco_Order")
                .usingGeneratedKeyColumns("id");
        this.orderTacoInserter = new SimpleJdbcInsert(jdbc)
                .withTableName("Taco_Order_Tacos");
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Order save(Order order) {
        order.setPlacedAt(LocalDate.now());
        order.setId(saveOrderDetails(order));
        order.getTacos().forEach(it -> saveTacoToOrder(it.getId(), order.getId()));
        return order;
    }

    private long saveOrderDetails(Order order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> values = objectMapper.convertValue(order, Map.class);
        return orderInserter.executeAndReturnKey(values).longValue();
    }

    private void saveTacoToOrder(long tacoId, long orderId) {
        Map<String, Object> values = new HashMap<>();
        values.put("tacoOrder", orderId);
        values.put("taco", tacoId);
        orderTacoInserter.execute(values);
    }
}
