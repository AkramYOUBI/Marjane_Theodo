package com.nimbleways.springboilerplate.contollers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/orders")
public class MyController {
    @Autowired
    private ProductService ps;

    @Autowired
    private ProductRepository pr;

    @Autowired
    private OrderRepository or;

    /*@PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = or.findById(orderId).get();
        System.out.println(order);
        List<Long> ids = new ArrayList<>();
        ids.add(orderId);
        Set<Product> products = order.getItems();
        for (Product p : products) {
            if (p.getType().equals("NORMAL")) {
                if (p.getAvailable() > 0) {
                    p.setAvailable(p.getAvailable() - 1);
                    pr.save(p);
                } else {
                    int leadTime = p.getLeadTime();
                    if (leadTime > 0) {
                        ps.notifyDelay(leadTime, p);
                    }
                }
            } else if (p.getType().equals("SEASONAL")) {
                // Add new season rules
                if ((LocalDate.now().isAfter(p.getSeasonStartDate()) && LocalDate.now().isBefore(p.getSeasonEndDate())
                        && p.getAvailable() > 0)) {
                    p.setAvailable(p.getAvailable() - 1);
                    pr.save(p);
                } else {
                    ps.handleSeasonalProduct(p);
                }
            } else if (p.getType().equals("EXPIRABLE")) {
                if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
                    p.setAvailable(p.getAvailable() - 1);
                    pr.save(p);
                } else {
                    ps.handleExpiredProduct(p);
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }*/

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = or.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        System.out.println(order);

        LocalDate today = LocalDate.now();

        order.getItems().forEach(p -> {
            switch (p.getType()) {
                case "NORMAL":
                    processNormalProduct(p);
                    break;

                case "SEASONAL":
                    processSeasonalProduct(p, today);
                    break;

                case "EXPIRABLE":
                    processExpirableProduct(p, today);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown product type: " + p.getType());
            }
        });

        return new ProcessOrderResponse(order.getId());
    }

    private void processNormalProduct(Product p) {
        if (p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else if (p.getLeadTime() > 0) {
            ps.notifyDelay(p.getLeadTime(), p);
        }
    }

    private void processSeasonalProduct(Product p, LocalDate today) {
        if (isWithinSeason(p, today) && p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ps.handleSeasonalProduct(p);
        }
    }

    private void processExpirableProduct(Product p, LocalDate today) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(today)) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ps.handleExpiredProduct(p);
        }
    }

    private boolean isWithinSeason(Product p, LocalDate today) {
        return today.isAfter(p.getSeasonStartDate()) && today.isBefore(p.getSeasonEndDate());
    }

}
