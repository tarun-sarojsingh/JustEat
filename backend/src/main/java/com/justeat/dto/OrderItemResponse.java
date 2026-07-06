package com.justeat.dto;

import com.justeat.model.OrderItem;
import java.math.BigDecimal;

public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse r = new OrderItemResponse();
        r.id = item.getId();
        if (item.getMenuItem() != null) {
            r.menuItemId = item.getMenuItem().getId();
            r.menuItemName = item.getMenuItem().getName();
        }
        r.quantity = item.getQuantity();
        r.unitPrice = item.getUnitPrice();
        r.lineTotal = (item.getUnitPrice() == null)
                ? BigDecimal.ZERO
                : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return r;
    }

    public Long getId() { return id; }
    public Long getMenuItemId() { return menuItemId; }
    public String getMenuItemName() { return menuItemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
